package com.appcoins.wallet.gamification.repository

import com.appcoins.wallet.core.network.backend.model.PromotionsResponse
import com.appcoins.wallet.core.network.backend.model.WalletOrigin
import com.google.gson.annotations.SerializedName

data class UserStats(val promotions: List<PromotionsResponse>,
                     @SerializedName("wallet_origin") val walletOrigin: WalletOrigin,
                     val error: Status? = null,
                     val fromCache: Boolean = false) {

  constructor(error: Status, fromCache: Boolean = false) : this(emptyList(), WalletOrigin.UNKNOWN,
      error, fromCache)
}

enum class Status {
  NO_NETWORK, UNKNOWN_ERROR
}
