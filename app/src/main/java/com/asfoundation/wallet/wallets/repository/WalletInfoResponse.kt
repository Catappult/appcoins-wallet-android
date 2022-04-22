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
  @SerializedName("sentry_breadcrumbs") val breadcrumbs: Int
)