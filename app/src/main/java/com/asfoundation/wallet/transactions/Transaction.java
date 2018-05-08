package com.asfoundation.wallet.transactions;

import com.asfoundation.wallet.entity.RawTransaction;
import javax.annotation.Nullable;

public class Transaction {
  private final String transactionId;
  @Nullable private final String approveTransactionId;
  private final TransactionType type;
  private final long timeStamp;
  private final TransactionStatus status;
  private final String value;
  private final String from;
  private final String to;
  private final String details;
  private final String currency;
  private final RawTransaction transaction;

  public Transaction(String transactionId, TransactionType type,
      @Nullable String approveTransactionId, long timeStamp, TransactionStatus status,
      String value, String from, String to, String details, String currency, RawTransaction transaction) {
    this.transactionId = transactionId;
    this.approveTransactionId = approveTransactionId;
    this.type = type;
    this.timeStamp = timeStamp;
    this.status = status;
    this.value = value;
    this.from = from;
    this.to = to;
    this.details = details;
    this.currency = currency;
    this.transaction = transaction;
  }

  public TransactionType getTransactionType() {
    return type;
  }

  @Override public int hashCode() {
    int result = transactionId.hashCode();
    result = 31 * result + (approveTransactionId != null ? approveTransactionId.hashCode() : 0);
    result = 31 * result + type.hashCode();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Transaction)) return false;

    Transaction that = (Transaction) o;

    if (!transactionId.equals(that.transactionId)) return false;
    if (approveTransactionId != null ? !approveTransactionId.equals(that.approveTransactionId)
        : that.approveTransactionId != null) {
      return false;
    }
    return type == that.type;
  }

  public String getApproveTransactionId() {
    return approveTransactionId;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public TransactionType getType() {
    return type;
  }

  public TransactionStatus getStatus() {
    return status;
  }

  public String getValue() {
    return value;
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }

  public String getDetails() {
    return details;
  }

  public RawTransaction getTransaction() {
    return transaction;
  }

  public String getCurrency() {
    return currency;
  }

  public enum TransactionType {
    STANDARD, IAB, ADS
  }

  public enum TransactionStatus {
    SUCCESS, FAILED, PENDING
  }

  @Override public String toString() {
    return "Transaction{"
        + "transactionId='"
        + transactionId
        + '\''
        + ", approveTransactionId='"
        + approveTransactionId
        + '\''
        + ", type="
        + type
        + ", timeStamp="
        + timeStamp
        + ", status="
        + status
        + ", value='"
        + value
        + '\''
        + ", from='"
        + from
        + '\''
        + ", to='"
        + to
        + '\''
        + ", details='"
        + details
        + '\''
        + ", currency='"
        + currency
        + '\''
        + ", transaction="
        + transaction
        + '}';
  }
}
