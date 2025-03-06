package com.asfoundation.wallet.promotions.model

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet

data class PromotionsModel(
  val promotions: List<Promotion>,
  val perks: List<PerkPromotion>,
  val partnerPerk: PartnerPerk?,
  val maxBonus: Double,
  val wallet: Wallet,
  val walletOrigin: WalletOrigin,
  val error: Status? = null,
  val fromCache: Boolean = false,
  val vipReferralInfo: VipReferralInfo?
) {

  fun hasError() = error != null

  enum class WalletOrigin {
    UNKNOWN, APTOIDE, PARTNER, PARTNER_NO_BONUS
  }

  enum class Status {
    NO_NETWORK, UNKNOWN_ERROR
  }
}



