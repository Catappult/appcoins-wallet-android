package com.asfoundation.wallet.transactions;

public class Transaction {
  private final String transactionId;

  public Transaction(String id) {
    this.transactionId = id;
  }

  @Override public int hashCode() {
    return transactionId.hashCode();
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Transaction)) return false;

    Transaction that = (Transaction) o;

    return transactionId.equals(that.transactionId);
  }

  @Override public String toString() {
    return "Transaction{" + "transactionId='" + transactionId + '\'' + '}';
  }

  public String getTransactionId() {
    return transactionId;
  }
}
