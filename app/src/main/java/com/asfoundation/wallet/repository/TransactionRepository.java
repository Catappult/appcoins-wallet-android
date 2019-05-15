package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.RawTransaction;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.asfoundation.wallet.poa.BlockchainErrorMapper;
import com.asfoundation.wallet.service.AccountKeystoreService;
import com.asfoundation.wallet.service.TransactionsNetworkClientType;
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer;
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
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import static com.asfoundation.wallet.C.ETHEREUM_NETWORK_NAME;
import static com.asfoundation.wallet.C.ROPSTEN_NETWORK_NAME;

public class TransactionRepository implements TransactionRepositoryType {

  private final NetworkInfo defaultNetwork;
  private final AccountKeystoreService accountKeystoreService;
  private final TransactionLocalSource inDiskCache;
  private final TransactionsNetworkClientType blockExplorerClient;
  private final DefaultTokenProvider defaultTokenProvider;
  private final BlockchainErrorMapper errorMapper;
  private final MultiWalletNonceObtainer nonceObtainer;
  private final Scheduler scheduler;

  public TransactionRepository(NetworkInfo defaultNetwork,
      AccountKeystoreService accountKeystoreService, TransactionLocalSource inDiskCache,
      TransactionsNetworkClientType blockExplorerClient, DefaultTokenProvider defaultTokenProvider,
      BlockchainErrorMapper errorMapper, MultiWalletNonceObtainer nonceObtainer,
      Scheduler scheduler) {
    this.defaultNetwork = defaultNetwork;
    this.accountKeystoreService = accountKeystoreService;
    this.blockExplorerClient = blockExplorerClient;
    this.inDiskCache = inDiskCache;
    this.defaultTokenProvider = defaultTokenProvider;
    this.errorMapper = errorMapper;
    this.nonceObtainer = nonceObtainer;
    this.scheduler = scheduler;
  }

  @Override public Observable<RawTransaction[]> fetchTransaction(Wallet wallet) {
    return Single.merge(fetchFromCache(defaultNetwork, wallet),
        fetchAndCacheFromNetwork(defaultNetwork, wallet))
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
    return createTransactionAndSend(transactionBuilder, password, transactionBuilder.data(),
        transactionBuilder.shouldSendToken() ? transactionBuilder.contractAddress()
            : transactionBuilder.toAddress(), transactionBuilder.shouldSendToken() ? BigDecimal.ZERO
            : transactionBuilder.subunitAmount());
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
        nonceObtainer.getNonce(new Address(transactionBuilder.fromAddress()),
            getChainId(transactionBuilder))).map(
        signedTransaction -> Numeric.toHexString(new Transaction(signedTransaction).getHash()));
  }

  @Override public Single<String> computeBuyTransactionHash(TransactionBuilder transactionBuilder,
      String password) {
    return defaultTokenProvider.getDefaultToken()
        .observeOn(scheduler)
        .flatMap(tokenInfo -> createRawTransaction(transactionBuilder, password,
            transactionBuilder.appcoinsData(), transactionBuilder.getIabContract(), BigDecimal.ZERO,
            nonceObtainer.getNonce(new Address(transactionBuilder.fromAddress()),
                getChainId(transactionBuilder))))
        .map(
            signedTransaction -> Numeric.toHexString(new Transaction(signedTransaction).getHash()));
  }

  private Single<String> createTransactionAndSend(TransactionBuilder transactionBuilder,
      String password, byte[] data, String toAddress, BigDecimal amount) {
    final Web3j web3j = Web3jFactory.build(new HttpService(defaultNetwork.rpcServerUrl));
    return Single.fromCallable(
        () -> nonceObtainer.getNonce(new Address(transactionBuilder.fromAddress()),
            getChainId(transactionBuilder)))
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
            .doOnSuccess(hash -> nonceObtainer.consumeNonce(nonceValue,
                new Address(transactionBuilder.fromAddress()), getChainId(transactionBuilder)))
            .retryWhen(throwableFlowable -> throwableFlowable.flatMap(
                throwable -> getPublisher(throwable, nonceValue, transactionBuilder))))
        .retryWhen(throwableFlowable -> throwableFlowable.flatMap(this::retry));
  }

  private long getChainId(TransactionBuilder transactionBuilder) {
    return transactionBuilder.getChainId() == TransactionBuilder.NO_CHAIN_ID
        ? defaultNetwork.chainId : transactionBuilder.getChainId();
  }

  private Single<byte[]> createRawTransaction(TransactionBuilder transactionBuilder,
      String password, byte[] data, String toAddress, BigDecimal amount, BigInteger nonce) {
    return Single.just(nonce)
        .flatMap(__ -> {
          if (transactionBuilder.getChainId() != TransactionBuilder.NO_CHAIN_ID
              && transactionBuilder.getChainId() != defaultNetwork.chainId) {
            String requestedNetwork = "unknown";
            if (transactionBuilder.getChainId() == 1) {
              requestedNetwork = ETHEREUM_NETWORK_NAME;
            } else if (transactionBuilder.getChainId() == 3) {
              requestedNetwork = ROPSTEN_NETWORK_NAME;
            }
            return Single.error(new WrongNetworkException(
                "Default network is different from the intended on transaction\nCurrent network: "
                    + defaultNetwork.name
                    + "\nRequested: "
                    + requestedNetwork));
          }
          return accountKeystoreService.signTransaction(transactionBuilder.fromAddress(), password,
              toAddress, amount, transactionBuilder.gasSettings().gasPrice,
              transactionBuilder.gasSettings().gasLimit, nonce.longValue(), data,
              defaultNetwork.chainId);
        });
  }

  private Publisher<?> retry(Throwable throwable) {
    if (isNonceError(throwable)) {
      return Flowable.just(true);
    }
    return Flowable.error(throwable);
  }

  private Publisher<?> getPublisher(Throwable throwable, BigInteger nonceValue,
      TransactionBuilder transactionBuilder) {
    if (isNonceError(throwable)) {
      nonceObtainer.consumeNonce(nonceValue, new Address(transactionBuilder.fromAddress()),
          getChainId(transactionBuilder));
    }
    return Flowable.error(throwable);
  }

  private boolean isNonceError(Throwable throwable) {
    return errorMapper.map(throwable)
        .equals(BlockchainErrorMapper.BlockchainError.NONCE_ERROR);
  }

  private Single<RawTransaction[]> fetchFromCache(NetworkInfo networkInfo, Wallet wallet) {
    return inDiskCache.fetchTransaction(networkInfo, wallet);
  }

  private Single<RawTransaction[]> fetchAndCacheFromNetwork(NetworkInfo networkInfo,
      Wallet wallet) {
    return inDiskCache.findLast(networkInfo, wallet)
        .flatMap(lastTransaction -> Single.fromObservable(
            blockExplorerClient.fetchLastTransactions(wallet, lastTransaction, networkInfo)))
        .onErrorResumeNext(throwable -> Single.fromObservable(
            blockExplorerClient.fetchLastTransactions(wallet, null, networkInfo)))
        .flatMapCompletable(
            transactions -> inDiskCache.putTransactions(networkInfo, wallet, transactions))
        .andThen(inDiskCache.fetchTransaction(networkInfo, wallet));
  }
}
