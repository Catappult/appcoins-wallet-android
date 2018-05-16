package com.asfoundation.wallet.ui.iab.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity public class InAppPurchaseData {
  @NonNull @PrimaryKey @ColumnInfo(name = "transaction_id") private final String transactionId;
  @NonNull @ColumnInfo(name = "package_name") private final String packageName;
  @NonNull @ColumnInfo(name = "application_name") private final String applicationName;
  @NonNull @ColumnInfo(name = "icon_path") private final String iconPath;
  @NonNull @ColumnInfo(name = "product_name") private final String productName;

  public InAppPurchaseData(@NonNull String transactionId, @NonNull String packageName,
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
    if (!(o instanceof InAppPurchaseData)) return false;

    InAppPurchaseData that = (InAppPurchaseData) o;

    if (!transactionId.equals(that.transactionId)) return false;
    if (!packageName.equals(that.packageName)) return false;
    if (!applicationName.equals(that.applicationName)) return false;
    if (!iconPath.equals(that.iconPath)) return false;
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
        + iconPath
        + '\''
        + ", productName='"
        + productName
        + '\''
        + '}';
  }
}
