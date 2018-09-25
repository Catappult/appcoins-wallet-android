package com.asfoundation.wallet.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

/**
 * Created by trinkes on 3/16/18.
 */

public class ApproveService {
  private final WatchedTransactionService transactionService;
  private final TransactionValidator approveTransactionSender;

  public ApproveService(WatchedTransactionService transactionService,
      TransactionValidator approveTransactionSender) {
    this.transactionService = transactionService;
    this.approveTransactionSender = approveTransactionSender;
  }

  public void start() {
    transactionService.start();
  }

  public Completable approve(String key, PaymentTransaction paymentTransaction) {
    return approveTransactionSender.validate(paymentTransaction)
        .andThen(
            transactionService.sendTransaction(key, paymentTransaction.getTransactionBuilder()));
  }

  public Observable<ApproveTransaction> getApprove(String uri) {
    return transactionService.getTransaction(uri)
        .map(this::map);
  }

  private ApproveTransaction map(Transaction transaction) {
    return new ApproveTransaction(transaction.getKey(),
        mapTransactionState(transaction.getStatus()), transaction.getTransactionHash());
  }

  private Status mapTransactionState(Transaction.Status status) {
    Status toReturn;
    switch (status) {
      case PENDING:
        toReturn = Status.PENDING;
        break;
      case PROCESSING:
        toReturn = Status.APPROVING;
        break;
      case COMPLETED:
        toReturn = Status.APPROVED;
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

  public Observable<List<ApproveTransaction>> getAll() {
    return transactionService.getAll()
        .flatMapSingle(transactions -> Observable.fromIterable(transactions)
            .map(this::map)
            .toList());
  }

  public Completable remove(String key) {
    return transactionService.remove(key);
  }

  public enum Status {
    PENDING, APPROVING, APPROVED, ERROR, WRONG_NETWORK, NONCE_ERROR, UNKNOWN_TOKEN, NO_TOKENS, NO_ETHER, NO_FUNDS, NO_INTERNET
  }

  public class ApproveTransaction {
    private final String key;
    private final Status status;
    private final String transactionHash;

    public ApproveTransaction(String key, Status status, String transactionHash) {
      this.key = key;
      this.status = status;
      this.transactionHash = transactionHash;
    }

    public String getKey() {
      return key;
    }

    public Status getStatus() {
      return status;
    }

    public String getTransactionHash() {
      return transactionHash;
    }
  }
}
