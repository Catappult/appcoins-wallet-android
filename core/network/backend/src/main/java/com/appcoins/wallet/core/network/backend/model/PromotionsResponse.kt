package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName


open class PromotionsResponse(
  val id: String,
  val priority: Int,
  @SerializedName("vip_onboarded") val vipOnboarded: VipOnboardedStatus?,
  @SerializedName("gamification_status") val gamificationStatus: GamificationStatus?,
) {
  enum class VipOnboardedStatus {
    VIP_NOT_ONBOARDED,
    VIP_ONBOARDED,
    NOT_VIP_USER;

    companion object {
      fun toEnum(value: String?) =
        VipOnboardedStatus.entries.firstOrNull { it.name == value } ?: NOT_VIP_USER
    }
  }

  enum class Status {
    ACTIVE,
    INACTIVE
  }
}