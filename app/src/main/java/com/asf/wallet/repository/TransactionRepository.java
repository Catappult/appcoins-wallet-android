package com.asf.wallet.repository;

import com.asf.wallet.entity.NetworkInfo;
import com.asf.wallet.entity.Transaction;
import com.asf.wallet.entity.TransactionBuilder;
import com.asf.wallet.entity.Wallet;
import com.asf.wallet.service.AccountKeystoreService;
import com.asf.wallet.service.TransactionsNetworkClientType;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

public class TransactionRepository implements TransactionRepositoryType {

  private final EthereumNetworkRepositoryType networkRepository;
  private final AccountKeystoreService accountKeystoreService;
  private final TransactionLocalSource inDiskCache;
  private final TransactionsNetworkClientType blockExplorerClient;

  public TransactionRepository(EthereumNetworkRepositoryType networkRepository,
      AccountKeystoreService accountKeystoreService, TransactionLocalSource inDiskCache,
      TransactionsNetworkClientType blockExplorerClient) {
    this.networkRepository = networkRepository;
    this.accountKeystoreService = accountKeystoreService;
    this.blockExplorerClient = blockExplorerClient;
    this.inDiskCache = inDiskCache;
  }

  @Override public Observable<Transaction[]> fetchTransaction(Wallet wallet) {
    NetworkInfo networkInfo = networkRepository.getDefaultNetwork();
    return Single.merge(fetchFromCache(networkInfo, wallet),
        fetchAndCacheFromNetwork(networkInfo, wallet))
        .toObservable();
  }

  @Override public Maybe<Transaction> findTransaction(Wallet wallet, String transactionHash) {
    return fetchTransaction(wallet).firstElement()
        .flatMap(transactions -> {
          for (Transaction transaction : transactions) {
            if (transaction.hash.equals(transactionHash)) {
              return Maybe.just(transaction);
            }
          }
          return null;
        });
  }

  public Single<String> createTransaction(TransactionBuilder transactionBuilder, String password) {
    return createTransaction(transactionBuilder, password, transactionBuilder.data(),
        transactionBuilder.shouldSendToken() ? transactionBuilder.contractAddress()
            : transactionBuilder.toAddress());
  }

  @Override public Single<String> approve(TransactionBuilder transactionBuilder, String password,
      String spender) {
    return createTransaction(transactionBuilder, password, transactionBuilder.approveData(spender),
        transactionBuilder.shouldSendToken() ? transactionBuilder.contractAddress()
            : transactionBuilder.toAddress());
  }

  @Override public Single<String> callIab(TransactionBuilder transaction, String password,
      String contractAddress) {
    //hacking
    return createTransaction(transaction, password, transaction.buyData(), contractAddress);
  }

  private Single<String> createTransaction(TransactionBuilder transactionBuilder, String password,
      byte[] data, String toAddress) {
    final Web3j web3j =
        Web3jFactory.build(new HttpService(networkRepository.getDefaultNetwork().rpcServerUrl));

    return getNonce(web3j, transactionBuilder.fromAddress()).flatMap(nonce -> {
      if (transactionBuilder.getChainId() != TransactionBuilder.NO_CHAIN_ID
          && transactionBuilder.getChainId() != networkRepository.getDefaultNetwork().chainId) {
        String requestedNetwork = "unknown";
        for (NetworkInfo networkInfo : networkRepository.getAvailableNetworkList()) {
          if (networkInfo.chainId == transactionBuilder.getChainId()) {
            requestedNetwork = networkInfo.name;
            break;
          }
        }
        return Single.error(new RuntimeException(
            "Default network is different from the intended on transaction\nCurrent network: "
                + networkRepository.getDefaultNetwork().name
                + "\nRequested: "
                + requestedNetwork));
      }
      return accountKeystoreService.signTransaction(transactionBuilder.fromAddress(), password,
          toAddress, transactionBuilder.shouldSendToken() ? BigDecimal.ZERO
              : transactionBuilder.subunitAmount(), transactionBuilder.gasSettings().gasPrice,
          transactionBuilder.gasSettings().gasLimit, nonce.longValue(), data,
          networkRepository.getDefaultNetwork().chainId);
    })
        .flatMap(signedMessage -> Single.fromCallable(() -> {
          EthSendTransaction raw = web3j.ethSendRawTransaction(Numeric.toHexString(signedMessage))
              .send();
          if (raw.hasError()) {
            throw new Exception(raw.getError()
                .getMessage());
          }
          return raw.getTransactionHash();
        }))
        .subscribeOn(Schedulers.io());
  }

  private Single<BigInteger> getNonce(Web3j web3j, String fromAddress) {
    return Single.fromCallable(() -> {
      EthGetTransactionCount ethGetTransactionCount =
          web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
              .send();
      return ethGetTransactionCount.getTransactionCount();
    });
  }

  private Single<Transaction[]> fetchFromCache(NetworkInfo networkInfo, Wallet wallet) {
    return inDiskCache.fetchTransaction(networkInfo, wallet);
  }

  private Single<Transaction[]> fetchAndCacheFromNetwork(NetworkInfo networkInfo, Wallet wallet) {
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
