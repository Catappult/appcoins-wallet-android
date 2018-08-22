package com.asfoundation.wallet.repository;

import com.asf.microraidenj.type.Address;
import com.asf.microraidenj.type.ByteArray;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.poa.BlockchainErrorMapper;
import com.asfoundation.wallet.service.AccountKeystoreService;
import com.asfoundation.wallet.service.TransactionsNetworkClientType;
import com.asfoundation.wallet.ui.iab.raiden.NonceObtainer;
import ethereumj.Transaction;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.reactivestreams.Publisher;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

public class TransactionRepository implements TransactionRepositoryType {

  private final EthereumNetworkRepositoryType networkRepository;
  private final AccountKeystoreService accountKeystoreService;
  private final TransactionLocalSource inDiskCache;
  private final TransactionsNetworkClientType blockExplorerClient;
  private final DefaultTokenProvider defaultTokenProvider;
  private final NonceGetter nonceGetter;
  private final BlockchainErrorMapper errorMapper;
  private final NonceObtainer nonceObtainer;
  private final Scheduler scheduler;

  public TransactionRepository(EthereumNetworkRepositoryType networkRepository,
      AccountKeystoreService accountKeystoreService, TransactionLocalSource inDiskCache,
      TransactionsNetworkClientType blockExplorerClient, DefaultTokenProvider defaultTokenProvider,
      NonceGetter nonceGetter, BlockchainErrorMapper errorMapper, NonceObtainer nonceObtainer,
      Scheduler scheduler) {
    this.networkRepository = networkRepository;
    this.accountKeystoreService = accountKeystoreService;
    this.blockExplorerClient = blockExplorerClient;
    this.inDiskCache = inDiskCache;
    this.defaultTokenProvider = defaultTokenProvider;
    this.nonceGetter = nonceGetter;
    this.errorMapper = errorMapper;
    this.nonceObtainer = nonceObtainer;
    this.scheduler = scheduler;
  }

  @Override public Observable<RawTransaction[]> fetchTransaction(Wallet wallet) {
    NetworkInfo networkInfo = networkRepository.getDefaultNetwork();
    return Single.merge(fetchFromCache(networkInfo, wallet),
        fetchAndCacheFromNetwork(networkInfo, wallet))
        .toObservable();
  }

  @Override public Maybe<RawTransaction> findTransaction(Wallet wallet, String transactionHash) {
    return fetchTransaction(wallet).firstElement()
        .flatMap(transactions -> {
          for (RawTransaction transaction : transactions) {
            if (transaction.hash.equals(transactionHash)) {
              return Maybe.just(transaction);
            }
          }
          return null;
        });
  }

  public Single<String> createTransaction(TransactionBuilder transactionBuilder, String password) {
    return nonceGetter.getNonce(transactionBuilder.fromAddress())
        .flatMap(nonce -> createTransactionAndSend(transactionBuilder, password,
            transactionBuilder.data(),
            transactionBuilder.shouldSendToken() ? transactionBuilder.contractAddress()
                : transactionBuilder.toAddress(),
            transactionBuilder.shouldSendToken() ? BigDecimal.ZERO
                : transactionBuilder.subunitAmount()));
  }

  @Override public Single<String> approve(TransactionBuilder transactionBuilder, String password) {
    return createTransactionAndSend(transactionBuilder, password, transactionBuilder.approveData(),
        transactionBuilder.contractAddress(), BigDecimal.ZERO);
  }

  @Override public Single<String> callIab(TransactionBuilder transaction, String password) {
    return defaultTokenProvider.getDefaultToken()
        .observeOn(scheduler)
        .flatMap(
            token -> createTransactionAndSend(transaction, password, transaction.appcoinsData(),
                transaction.getIabContract(), BigDecimal.ZERO));
  }

  @Override
  public Single<String> computeApproveTransactionHash(TransactionBuilder transactionBuilder,
      String password) {
    return createRawTransaction(transactionBuilder, password, transactionBuilder.approveData(),
        transactionBuilder.contractAddress(), BigDecimal.ZERO,
        nonceObtainer.getNonce(new Address(ByteArray.from(transactionBuilder.fromAddress())))).map(
        signedTransaction -> Numeric.toHexString(new Transaction(signedTransaction).getHash()));
  }

  @Override public Single<String> computeBuyTransactionHash(TransactionBuilder transactionBuilder,
      String password) {
    return defaultTokenProvider.getDefaultToken()
        .flatMap(tokenInfo -> createRawTransaction(transactionBuilder, password,
            transactionBuilder.appcoinsData(), transactionBuilder.getIabContract(),
            BigDecimal.ZERO,
            nonceObtainer.getNonce(new Address(ByteArray.from(transactionBuilder.fromAddress())))))
        .map(
            signedTransaction -> Numeric.toHexString(new Transaction(signedTransaction).getHash()));
  }

  private Single<String> createTransactionAndSend(TransactionBuilder transactionBuilder,
      String password, byte[] data, String toAddress, BigDecimal amount) {
    final Web3j web3j =
        Web3jFactory.build(new HttpService(networkRepository.getDefaultNetwork().rpcServerUrl));
    return Single.fromCallable(
        () -> nonceObtainer.getNonce(new Address(ByteArray.from(transactionBuilder.fromAddress()))))
        .flatMap(nonceValue -> createRawTransaction(transactionBuilder, password, data, toAddress,
            amount, nonceValue).flatMap(signedMessage -> Single.fromCallable(() -> {
          EthSendTransaction raw = web3j.ethSendRawTransaction(Numeric.toHexString(signedMessage))
              .send();
          if (raw.hasError()) {
            throw new TransactionException(raw.getError()
                .getCode(), raw.getError()
                .getMessage(), raw.getError()
                .getData());
          }
          return raw.getTransactionHash();
        })
            .subscribeOn(Schedulers.io()))
            .doOnSuccess(hash -> nonceObtainer.consumeNonce(nonceValue))
            .retryWhen(throwableFlowable -> throwableFlowable.flatMap(
                throwable -> getPublisher(throwable, nonceValue))));
  }

  private Single<byte[]> createRawTransaction(TransactionBuilder transactionBuilder,
      String password, byte[] data, String toAddress, BigDecimal amount, BigInteger nonce) {
    return Single.just(nonce)
        .flatMap(__ -> {
          if (transactionBuilder.getChainId() != TransactionBuilder.NO_CHAIN_ID
              && transactionBuilder.getChainId() != networkRepository.getDefaultNetwork().chainId) {
            String requestedNetwork = "unknown";
            for (NetworkInfo networkInfo : networkRepository.getAvailableNetworkList()) {
              if (networkInfo.chainId == transactionBuilder.getChainId()) {
                requestedNetwork = networkInfo.name;
                break;
              }
            }
            return Single.error(new WrongNetworkException(
                "Default network is different from the intended on transaction\nCurrent network: "
                    + networkRepository.getDefaultNetwork().name
                    + "\nRequested: "
                    + requestedNetwork));
          }
          return accountKeystoreService.signTransaction(transactionBuilder.fromAddress(), password,
              toAddress, amount, transactionBuilder.gasSettings().gasPrice,
              transactionBuilder.gasSettings().gasLimit, nonce.longValue(), data,
              networkRepository.getDefaultNetwork().chainId);
        });
  }

  private Publisher<?> getPublisher(Throwable throwable, BigInteger nonceValue) {
    if (errorMapper.map(throwable)
        .equals(BlockchainErrorMapper.BlockchainError.NONCE_ERROR)) {
      nonceObtainer.consumeNonce(nonceValue);
      return Flowable.just(true);
    }
    return Flowable.error(throwable);
  }

  private Single<RawTransaction[]> fetchFromCache(NetworkInfo networkInfo, Wallet wallet) {
    return inDiskCache.fetchTransaction(networkInfo, wallet);
  }

  private Single<RawTransaction[]> fetchAndCacheFromNetwork(NetworkInfo networkInfo,
      Wallet wallet) {
    return inDiskCache.findLast(networkInfo, wallet)
        .flatMap(lastTransaction -> Single.fromObservable(
            blockExplorerClient.fetchLastTransactions(wallet, lastTransaction)))
        .onErrorResumeNext(throwable -> Single.fromObservable(
            blockExplorerClient.fetchLastTransactions(wallet, null)))
        .flatMapCompletable(
            transactions -> inDiskCache.putTransactions(networkInfo, wallet, transactions))
        .andThen(inDiskCache.fetchTransaction(networkInfo, wallet));
  }
}
