package com.appcoins.wallet.gamification.repository.entity

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class ReferralResponse(
    @SerializedName("max_amount")
    val maxAmount: BigDecimal,
    val available: Int,
    val bundle: Boolean,
    val completed: Int,
    val currency: String,
    @SerializedName("sign")
    val symbol: String,
    val invited: Boolean,
    val link: String?,
    @SerializedName("pending_amount")
    val pendingAmount: BigDecimal,
    @SerializedName("received_amount")
    val receivedAmount: BigDecimal,
    @SerializedName("user_status")
    val userStatus: UserStatus?,
    val status: Status,
    val amount: BigDecimal) {

  enum class UserStatus {
    RESERVED, VERIFIED, REDEEMED
  }

  enum class Status {
    ACTIVE, INACTIVE
  }
}
