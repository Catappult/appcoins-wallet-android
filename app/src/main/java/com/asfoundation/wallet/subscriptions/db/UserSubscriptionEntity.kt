package com.asfoundation.wallet.subscriptions.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appcoins.wallet.core.network.microservices.model.SubscriptionSubStatus
import java.math.BigDecimal

@Entity
data class UserSubscriptionEntity(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "wallet_address") val walletAddress: String,
    val sku: String,
    val title: String,
    val period: String,
    @ColumnInfo(name = "sub_status") val subStatus: SubscriptionSubStatus,
    val started: String?,
    val renewal: String?,
    val expire: String?,
    val ended: String?,
    @ColumnInfo(name = "app_name") val appName: String,
    @ColumnInfo(name = "app_title") val appTitle: String,
    @ColumnInfo(name = "app_icon") val appIcon: String,
    val gateway: String,
    val reference: String,
    val value: BigDecimal,
    val label: String,
    val currency: String,
    val symbol: String,
    val created: String,
    @ColumnInfo(name = "method_name") val methodName: String,
    @ColumnInfo(name = "method_title") val methodTitle: String,
    @ColumnInfo(name = "method_logo") val methodLogo: String,
    @ColumnInfo(name = "appc_value") val appcValue: BigDecimal,
    @ColumnInfo(name = "appc_label") val appcLabel: String
)
