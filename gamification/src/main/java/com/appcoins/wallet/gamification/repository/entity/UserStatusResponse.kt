package com.appcoins.wallet.gamification.repository.entity

import com.google.gson.annotations.SerializedName

data class UserStatusResponse(val promotions: List<PromotionsResponse>,
                              @SerializedName("wallet_origin") val walletOrigin: WalletOrigin)

enum class WalletOrigin {
  UNKNOWN, APTOIDE, PARTNER
}
