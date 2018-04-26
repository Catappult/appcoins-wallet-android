package com.asfoundation.wallet.transactions;

class IabTransaction extends Transaction {
  private final String approveTransactionId;

  public IabTransaction(String iabTransactionId, String approveTransactionId) {
    super(iabTransactionId);
    this.approveTransactionId = approveTransactionId;
  }

  @Override public int hashCode() {
    return approveTransactionId.hashCode();
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IabTransaction)) return false;

    IabTransaction that = (IabTransaction) o;

    return approveTransactionId.equals(that.approveTransactionId);
  }

  @Override public String toString() {
    return "IabTransaction{"
        + "iabTransactionId"
        + getTransactionId()
        + ", approveTransactionId ="
        + approveTransactionId
        + +'}';
  }
}
