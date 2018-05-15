package com.asfoundation.wallet.ui.iab;

public class InAppPurchaseData {
  private final String transactionId;
  private final String packageName;
  private final String applicationName;
  private final String path;
  private final String productName;

  public InAppPurchaseData(String transactionId, String packageName, String applicationName,
      String path, String productName) {

    this.transactionId = transactionId;
    this.packageName = packageName;
    this.applicationName = applicationName;
    this.path = path;
    this.productName = productName;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public String getPath() {
    return path;
  }

  public String getProductName() {
    return productName;
  }

  public String getTransactionId() {
    return transactionId;
  }

  @Override public int hashCode() {
    int result = transactionId.hashCode();
    result = 31 * result + packageName.hashCode();
    result = 31 * result + applicationName.hashCode();
    result = 31 * result + path.hashCode();
    result = 31 * result + productName.hashCode();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InAppPurchaseData)) return false;

    InAppPurchaseData that = (InAppPurchaseData) o;

    if (!transactionId.equals(that.transactionId)) return false;
    if (!packageName.equals(that.packageName)) return false;
    if (!applicationName.equals(that.applicationName)) return false;
    if (!path.equals(that.path)) return false;
    return productName.equals(that.productName);
  }

  @Override public String toString() {
    return "InAppPurchaseData{"
        + "transactionId='"
        + transactionId
        + '\''
        + ", packageName='"
        + packageName
        + '\''
        + ", applicationName='"
        + applicationName
        + '\''
        + ", path='"
        + path
        + '\''
        + ", productName='"
        + productName
        + '\''
        + '}';
  }
}
