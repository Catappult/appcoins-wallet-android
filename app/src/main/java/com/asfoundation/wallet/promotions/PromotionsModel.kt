package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.repository.entity.Status

data class PromotionsModel(val promotions: List<Promotion>,
                           val maxBonus: Double,
                           val walletOrigin: WalletOrigin,
                           val error: Status? = null,
                           val fromCache: Boolean = false)

enum class WalletOrigin {
  UNKNOWN, APTOIDE, PARTNER
}

