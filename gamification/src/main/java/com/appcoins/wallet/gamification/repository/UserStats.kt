package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.gamification.repository.entity.PromotionsResponse
import com.google.gson.annotations.SerializedName
/*
data class UserStats(val promotions: List<PromotionsResponse>,
                     @SerializedName("wallet_origin") val walletOrigin: WalletOrigin,
                     val error: Status? = null,
                     val fromCache: Boolean = false) {

  constructor(error: Status) : this(emptyList(), WalletOrigin.UNKNOWN, error, false)

  constructor(error: Status, fromCache: Boolean) : this(emptyList(), WalletOrigin.UNKNOWN, error,
      fromCache)
}

enum class WalletOrigin {
  UNKNOWN, APTOIDE, PARTNER
}

enum class Status {
  NO_NETWORK, UNKNOWN_ERROR
}
*/