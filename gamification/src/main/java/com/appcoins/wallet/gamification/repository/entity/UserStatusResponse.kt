package com.appcoins.wallet.gamification.repository.entity

import com.google.gson.annotations.SerializedName

data class UserStatusResponse(
    @SerializedName("GAMIFICATION") val gamification: GamificationResponse,
    @SerializedName("REFERRAL") val referral: ReferralResponse?)
