package com.asfoundation.wallet.transactions;

import javax.annotation.Nullable;

public class Transaction {
  private final String transactionId;
  @Nullable private final String approveTransactionId;
  private final TransactionType transactionType;

  public Transaction(String transactionId, TransactionType transactionType,
      @Nullable String approveTransactionId) {
    this.transactionId = transactionId;
    this.approveTransactionId = approveTransactionId;
    this.transactionType = transactionType;
  }

  public Transaction(String transactionId) {
    this(transactionId, TransactionType.STANDARD, null);
  }

  public Transaction(String transactionId, String approveTransactionId) {
    this(transactionId, TransactionType.IAB, approveTransactionId);
  }

  public TransactionType getTransactionType() {
    return transactionType;
  }

  @Override public int hashCode() {
    int result = transactionId.hashCode();
    result = 31 * result + (approveTransactionId != null ? approveTransactionId.hashCode() : 0);
    result = 31 * result + transactionType.hashCode();
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
    return transactionType == that.transactionType;
  }

  @Override public String toString() {
    return "Transaction{"
        + "transactionId='"
        + transactionId
        + '\''
        + ", approveTransactionId='"
        + approveTransactionId
        + '\''
        + ", transactionType="
        + transactionType
        + '}';
  }

  public String getApproveTransactionId() {
    return approveTransactionId;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public enum TransactionType {
    STANDARD, IAB
  }
}
