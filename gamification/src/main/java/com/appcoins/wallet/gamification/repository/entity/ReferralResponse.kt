package com.appcoins.wallet.gamification.repository.entity

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class ReferralResponse(
    val available: Int,
    val completed: Int,
    val currency: String,
    val id: String,
    val invited: Int,
    @SerializedName("max_value")
    val maxValue: BigDecimal,
    @SerializedName("pending_value")
    val pendingValue: BigDecimal,
    @SerializedName("received_value")
    val received_value: BigDecimal,
    val status: Status,
    val value: BigDecimal) {

  enum class Status {
    RESERVED, VERIFIED, REDEEMED
  }

}
