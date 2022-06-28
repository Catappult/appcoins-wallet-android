package com.asfoundation.wallet.wallets.repository

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

data class WalletInfoResponse(
  val wallet: String,
  @SerializedName("eth_balance") val ethBalanceWei: BigInteger,
  @SerializedName("appc_balance") val appcBalanceWei: BigInteger,
  @SerializedName("appc_c_balance") val appcCreditsBalanceWei: BigInteger,
  val blocked: Boolean,
  val verified: Boolean,
  val logging: Boolean,
  @SerializedName("has_backup") val hasBackup: Boolean,
  @SerializedName("backup_date") val backupDate: Long?,
  @SerializedName("sentry_breadcrumbs") val breadcrumbs: Int
) {
  val syntheticBackupDate get() = backupDate ?: if (hasBackup) 1 else 0
}