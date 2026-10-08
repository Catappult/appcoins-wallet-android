package com.asfoundation.wallet.ui.iab.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppCoinsOperationEntity(
  @PrimaryKey @ColumnInfo(name = "key") val key: String,
  @ColumnInfo(name = "transaction_id") val transactionId: String,
  @ColumnInfo(name = "package_name") val packageName: String,
  @ColumnInfo(name = "application_name") val applicationName: String,
  @ColumnInfo(name = "icon_path") val iconPath: String,
  @ColumnInfo(name = "product_name") val productName: String
)
