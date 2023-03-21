package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class WithdrawBody(
  @SerializedName("target") val email: String,
  @SerializedName("usd_amount") val amount: BigDecimal
)
