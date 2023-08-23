package com.asfoundation.wallet.repository;

import com.appcoins.wallet.core.utils.android_common.RxSchedulers;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.interact.DefaultTokenProvider;
import com.appcoins.wallet.feature.walletInfo.data.AccountKeystoreService;
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.reactivestreams.Publisher;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import static com.appcoins.wallet.core.utils.jvm_common.C.ETHEREUM_NETWORK_NAME;
import static com.appcoins.wallet.core.utils.jvm_common.C.ROPSTEN_NETWORK_NAME;

public abstract class TransactionRepository implements TransactionRepositoryType {

  private final NetworkInfo defaultNetwork;
  private final AccountKeystoreService accountKeystoreService;
  private final DefaultTokenProvider defaultTokenProvider;
  private final BlockchainErrorMapper errorMapper;
  private final MultiWalletNonceObtainer nonceObtainer;
  private final RxSchedulers rxSchedulers;

  public TransactionRepository(NetworkInfo defaultNetwork,
      AccountKeystoreService accountKeystoreService, DefaultTokenProvider defaultTokenProvider,
      BlockchainErrorMapper errorMapper, MultiWalletNonceObtainer nonceObtainer,
      RxSchedulers rxSchedulers) {
    this.defaultNetwork = defaultNetwork;
    this.accountKeystoreService = accountKeystoreService;
    this.defaultTokenProvider = defaultTokenProvider;
    this.errorMapper = errorMapper;
    this.nonceObtainer = nonceObtainer;
    this.rxSchedulers = rxSchedulers;
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
        .observeOn(rxSchedulers.getIo())
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
            getChainId(transactionBuilder))).map(this::calculateHashFromSigned);
  }

  @Override public Single<String> computeBuyTransactionHash(TransactionBuilder transactionBuilder,
      String password) {
    return defaultTokenProvider.getDefaultToken()
        .observeOn(rxSchedulers.getIo())
        .flatMap(tokenInfo -> createRawTransaction(transactionBuilder, password,
            transactionBuilder.appcoinsData(), transactionBuilder.getIabContract(), BigDecimal.ZERO,
            nonceObtainer.getNonce(new Address(transactionBuilder.fromAddress()),
                getChainId(transactionBuilder))))
        .map(this::calculateHashFromSigned);
  }

  private Single<String> createTransactionAndSend(TransactionBuilder transactionBuilder,
      String password, byte[] data, String toAddress, BigDecimal amount) {
    final Web3j web3j = Web3j.build(new HttpService(defaultNetwork.rpcServerUrl));
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

  private String calculateHashFromSigned(byte[] signedTx) {
    byte[] hash = Hash.sha3(signedTx);
    return Numeric.toHexString(hash);
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
}
