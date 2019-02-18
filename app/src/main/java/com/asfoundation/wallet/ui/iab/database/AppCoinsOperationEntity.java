package com.asfoundation.wallet.ui.iab.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity public class AppCoinsOperationEntity {
  @NonNull @PrimaryKey @ColumnInfo(name = "key") private final String key;
  @NonNull @ColumnInfo(name = "transaction_id") private final String transactionId;
  @NonNull @ColumnInfo(name = "package_name") private final String packageName;
  @NonNull @ColumnInfo(name = "application_name") private final String applicationName;
  @NonNull @ColumnInfo(name = "icon_path") private final String iconPath;
  @NonNull @ColumnInfo(name = "product_name") private final String productName;

  public AppCoinsOperationEntity(@NonNull String key, @NonNull String transactionId,
      @NonNull String packageName, @NonNull String applicationName, @NonNull String iconPath,
      @NonNull String productName) {
    this.key = key;
    this.transactionId = transactionId;
    this.packageName = packageName;
    this.applicationName = applicationName;
    this.iconPath = iconPath;
    this.productName = productName;
  }

  @NonNull public String getKey() {
    return key;
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
    if (!(o instanceof AppCoinsOperationEntity)) return false;

    AppCoinsOperationEntity that = (AppCoinsOperationEntity) o;

    if (!key.equals(that.key)) return false;
    if (!transactionId.equals(that.transactionId)) return false;
    if (!packageName.equals(that.packageName)) return false;
    if (!applicationName.equals(that.applicationName)) return false;
    if (!iconPath.equals(that.iconPath)) return false;
    return productName.equals(that.productName);
  }

  @Override public String toString() {
    return "AppCoinsOperation{"
        + "key='"
        + key
        + '\''
        + ", transactionId='"
        + transactionId
        + '\''
        + ", packageName='"
        + packageName
        + '\''
        + ", applicationName='"
        + applicationName
        + '\''
        + ", iconPath='"
        + iconPath
        + '\''
        + ", productName='"
        + productName
        + '\''
        + '}';
  }
}
