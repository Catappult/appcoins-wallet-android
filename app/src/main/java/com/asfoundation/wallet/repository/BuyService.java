package com.asfoundation.wallet.repository;

import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by trinkes on 3/16/18.
 */

public class BuyService {
  private final WatchedTransactionService transactionService;
  private final TransactionValidator transactionValidator;

  public BuyService(WatchedTransactionService transactionService,
      TransactionValidator transactionValidator) {
    this.transactionService = transactionService;
    this.transactionValidator = transactionValidator;
  }

  public void start() {
    transactionService.start();
  }

  public Completable buy(String key, PaymentTransaction paymentTransaction) {
    return transactionValidator.validate(paymentTransaction)
        .andThen(transactionService.sendTransaction(key, paymentTransaction.getNonce()
            .add(BigInteger.ONE), paymentTransaction.getTransactionBuilder()));
  }

  public Observable<BuyTransaction> getBuy(String uri) {
    return transactionService.getTransaction(uri)
        .map(this::mapTransaction);
  }

  private BuyTransaction mapTransaction(Transaction transaction) {
    return new BuyTransaction(transaction.getKey(), transaction.getTransactionBuilder(),
        mapState(transaction.getStatus()), transaction.getTransactionHash(),
        transaction.getNonce());
  }

  private Status mapState(Transaction.Status status) {
    Status toReturn;
    switch (status) {
      case PENDING:
        toReturn = Status.PENDING;
        break;
      case PROCESSING:
        toReturn = Status.BUYING;
        break;
      case COMPLETED:
        toReturn = Status.BOUGHT;
        break;
      default:
      case ERROR:
        toReturn = Status.ERROR;
        break;
      case WRONG_NETWORK:
        toReturn = Status.WRONG_NETWORK;
        break;
      case NONCE_ERROR:
        toReturn = Status.NONCE_ERROR;
        break;
      case UNKNOWN_TOKEN:
        toReturn = Status.UNKNOWN_TOKEN;
        break;
      case NO_TOKENS:
        toReturn = Status.NO_TOKENS;
        break;
      case NO_ETHER:
        toReturn = Status.NO_ETHER;
        break;
      case NO_FUNDS:
        toReturn = Status.NO_FUNDS;
        break;
      case NO_INTERNET:
        toReturn = Status.NO_INTERNET;
        break;
    }
    return toReturn;
  }

  public Observable<List<BuyTransaction>> getAll() {
    return transactionService.getAll()
        .flatMapSingle(entries -> Observable.fromIterable(entries)
            .map(this::mapTransaction)
            .toList());
  }

  public Completable remove(String key) {
    return transactionService.remove(key);
  }

  public enum Status {
    BUYING, BOUGHT, ERROR, WRONG_NETWORK, NONCE_ERROR, UNKNOWN_TOKEN, NO_TOKENS, NO_ETHER,
    NO_FUNDS, NO_INTERNET, PENDING

  }

  public static class BuyTransaction {
    private final String key;
    private final TransactionBuilder transactionBuilder;
    private final Status status;
    private final String transactionHash;
    private final BigInteger nonce;

    private BuyTransaction(String key, TransactionBuilder transactionBuilder, Status status,
        String transactionHash, BigInteger nonce) {
      this.key = key;
      this.transactionBuilder = transactionBuilder;
      this.status = status;
      this.transactionHash = transactionHash;
      this.nonce = nonce;
    }

    public String getKey() {
      return key;
    }

    public TransactionBuilder getTransactionBuilder() {
      return transactionBuilder;
    }

    public Status getStatus() {
      return status;
    }

    public String getTransactionHash() {
      return transactionHash;
    }

    public BigInteger getNonce() {
      return nonce;
    }
  }
}
