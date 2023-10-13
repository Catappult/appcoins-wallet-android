package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

data class WalletInfoResponse(
  val wallet: String,
  @SerializedName("eth_balance") val ethBalanceWei: BigInteger,
  @SerializedName("appc_balance") val appcBalanceWei: BigInteger,
  @SerializedName("appc_c_balance") val appcCreditsBalanceWei: BigInteger,
  @SerializedName("eth_fiat_balance") val ethBalanceFiat: BigDecimal,
  @SerializedName("appc_fiat_balance") val appcBalanceFiat: BigDecimal,
  @SerializedName("appc_c_fiat_balance") val appcCreditsBalancFiat: BigDecimal,
  val currency: String,
  val symbol: String,
  val blocked: Boolean,
  val verified: Boolean,
  val logging: Boolean,
  @SerializedName("has_backup") val hasBackup: Boolean,
  @SerializedName("last_backup_date") val lastBackupDate: Date?,
  @SerializedName("sentry_breadcrumbs") val breadcrumbs: Int
) {
  val syntheticBackupDate get() = lastBackupDate?.time ?: if (hasBackup) 1 else 0
}
