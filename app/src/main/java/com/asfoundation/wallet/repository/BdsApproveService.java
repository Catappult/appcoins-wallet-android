package com.asfoundation.wallet.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;

/**
 * Created by trinkes on 3/16/18.
 */

public class BdsApproveService implements ApproveService {
  private final WatchedTransactionService transactionService;
  private final TransactionValidator approveTransactionSenderBds;
  private final TransactionValidator approveTransactionSenderOnChain;

  public BdsApproveService(WatchedTransactionService transactionService,
      TransactionValidator approveTransactionSenderBds,
      TransactionValidator approveTransactionSenderOnChain) {
    this.transactionService = transactionService;
    this.approveTransactionSenderBds = approveTransactionSenderBds;
    this.approveTransactionSenderOnChain = approveTransactionSenderOnChain;
  }

  @Override public void start() {
    transactionService.start();
  }

  @Override
  public Completable approve(String key, PaymentTransaction paymentTransaction, boolean useBds) {
    Completable validate = useBds ? approveTransactionSenderBds.validate(paymentTransaction)
        : approveTransactionSenderOnChain.validate(paymentTransaction);
    return validate.andThen(
        transactionService.sendTransaction(key, paymentTransaction.getTransactionBuilder()));
  }

  @Override public Observable<ApproveTransaction> getApprove(String uri) {
    return transactionService.getTransaction(uri)
        .map(this::map);
  }

  @Override public Observable<List<ApproveTransaction>> getAll() {
    return transactionService.getAll()
        .flatMapSingle(transactions -> Observable.fromIterable(transactions)
            .map(this::map)
            .toList());
  }

  @Override public Completable remove(String key) {
    return transactionService.remove(key);
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
