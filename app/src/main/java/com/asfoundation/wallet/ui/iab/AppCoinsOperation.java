package com.asfoundation.wallet.ui.iab;

import androidx.annotation.NonNull;

public class AppCoinsOperation {
  private final String transactionId;
  private final String packageName;
  private final String applicationName;
  private final String iconPath;
  private final String productName;

  public AppCoinsOperation(@NonNull String transactionId, @NonNull String packageName,
      @NonNull String applicationName, @NonNull String iconPath, @NonNull String productName) {
    this.transactionId = transactionId;
    this.packageName = packageName;
    this.applicationName = applicationName;
    this.iconPath = iconPath;
    this.productName = productName;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public String getIconPath() {
    return iconPath;
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
    result = 31 * result + iconPath.hashCode();
    result = 31 * result + productName.hashCode();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AppCoinsOperation)) return false;

    AppCoinsOperation that = (AppCoinsOperation) o;

    if (!transactionId.equals(that.transactionId)) return false;
    if (!packageName.equals(that.packageName)) return false;
    if (!applicationName.equals(that.applicationName)) return false;
    if (!iconPath.equals(that.iconPath)) return false;
    return productName.equals(that.productName);
  }

  @Override public String toString() {
    return "AppCoinsOperation{"
        + "transactionId='"
        + transactionId
        + '\''
        + ", packageName='"
        + packageName
        + '\''
        + ", applicationName='" + applicationName + '\'' + ", iconPath='"
        + iconPath
        + '\''
        + ", productName='"
        + productName
        + '\''
        + '}';
  }
}
