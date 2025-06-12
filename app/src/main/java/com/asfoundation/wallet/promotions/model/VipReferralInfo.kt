package com.asfoundation.wallet.promotions.model

import com.appcoins.wallet.core.network.backend.model.PromoCodeBonusResponse.App

data class VipReferralInfo(
  val vipBonus: String,
  val vipCode: String,
  val totalEarned: String,
  val totalEarnedConvertedCurrency: String,
  val numberReferrals: String,
  val endDate: Long,
  val startDate: Long,
  val active: Boolean,
  val app: App,
  val maxReward: String,
  val currencySymbol: String,
)
