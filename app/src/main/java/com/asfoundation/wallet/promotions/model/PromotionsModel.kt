package com.asfoundation.wallet.promotions.model

import com.asfoundation.wallet.entity.Wallet

data class PromotionsModel(val promotions: List<Promotion>,
                           val maxBonus: Double,
                           val wallet: Wallet,
                           val walletOrigin: WalletOrigin,
                           val error: Status? = null,
                           val fromCache: Boolean = false) {

  fun hasError() = error != null
}

enum class WalletOrigin {
  UNKNOWN, APTOIDE, PARTNER
}

enum class Status {
  NO_NETWORK, UNKNOWN_ERROR
}