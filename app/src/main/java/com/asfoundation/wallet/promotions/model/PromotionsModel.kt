package com.asfoundation.wallet.promotions.model

data class PromotionsModel(val promotions: List<Promotion>,
                           val vouchers: List<VoucherItem>,
                           val perks: List<PerkPromotion>,
                           val walletOrigin: WalletOrigin,
                           val error: Status? = null) {

  fun hasError() = error != null

  enum class WalletOrigin {
    UNKNOWN, APTOIDE, PARTNER
  }

  enum class Status {
    NO_NETWORK, UNKNOWN_ERROR
  }
}



