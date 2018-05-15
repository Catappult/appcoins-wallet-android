package com.asfoundation.wallet.ui.iab;

public class InAppPurchaseData {
  private final String transactionId;

  public InAppPurchaseData(String transactionId) {
    this.transactionId = transactionId;
  }

  @Override public int hashCode() {
    return transactionId != null ? transactionId.hashCode() : 0;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InAppPurchaseData)) return false;

    InAppPurchaseData that = (InAppPurchaseData) o;

    return transactionId != null ? transactionId.equals(that.transactionId)
        : that.transactionId == null;
  }

  public String getTransactionId() {
    return transactionId;
  }
}
