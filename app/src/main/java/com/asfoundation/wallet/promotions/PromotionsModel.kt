package com.asfoundation.wallet.promotions

data class PromotionsModel(val promotions: List<Promotion>,
                           val maxBonus: Double,
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