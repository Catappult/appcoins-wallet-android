package com.asfoundation.wallet.analytics

import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promotions.model.PromotionsModel


class JoinedAnalytics(
  private val rakamAnalytics: RakamAnalytics,
  private val indicativeAnalytics: IndicativeAnalytics,
  private val sentryAnalytics: SentryAnalytics
) :
  AnalyticsSetup {

  override fun setUserId(walletAddress: String) {
    rakamAnalytics.setUserId(walletAddress)
    indicativeAnalytics.setUserId(walletAddress)
    sentryAnalytics.setUserId(walletAddress)
  }

  override fun setGamificationLevel(level: Int) {
    rakamAnalytics.setGamificationLevel(level)
    indicativeAnalytics.setGamificationLevel(level)
    sentryAnalytics.setGamificationLevel(level)
  }

  override fun setWalletOrigin(origin: PromotionsModel.WalletOrigin) {
    rakamAnalytics.setWalletOrigin(origin)
    indicativeAnalytics.setWalletOrigin(origin)
    sentryAnalytics.setWalletOrigin(origin)
  }

  override fun setPromoCode(promoCode: PromoCode) {
    rakamAnalytics.setPromoCode(promoCode)
    indicativeAnalytics.setPromoCode(promoCode)
    sentryAnalytics.setPromoCode(promoCode)
  }
}
