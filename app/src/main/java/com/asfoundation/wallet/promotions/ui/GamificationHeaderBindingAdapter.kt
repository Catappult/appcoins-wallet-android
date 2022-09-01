package com.asfoundation.wallet.promotions.ui

import com.asf.wallet.databinding.PromotionsAlmostVipLevelHeaderBinding
import com.asf.wallet.databinding.PromotionsRegularLevelHeaderBinding
import com.asf.wallet.databinding.PromotionsVipLevelHeaderBinding
import com.asf.wallet.databinding.PromotionsVipMaxLevelHeaderBinding

class GamificationHeaderBindingAdapter(
  regularLevel: PromotionsRegularLevelHeaderBinding?,
  almostVipLevel: PromotionsAlmostVipLevelHeaderBinding?,
  vipLevelHeader: PromotionsVipLevelHeaderBinding?,
  maxLevelHeader: PromotionsVipMaxLevelHeaderBinding?
) {

  val type =
    regularLevel
      ?: almostVipLevel
      ?: vipLevelHeader
      ?: maxLevelHeader

  val currentLevelBonus =
    regularLevel?.currentLevelBonus
      ?: almostVipLevel?.currentLevelBonus
      ?: vipLevelHeader?.currentLevelBonus
      ?: maxLevelHeader?.currentLevelBonus

  val currentLevelTitle =
    regularLevel?.currentLevelTitle
      ?: almostVipLevel?.currentLevelTitle
      ?: vipLevelHeader?.currentLevelTitle
      ?: maxLevelHeader?.currentLevelTitle

  val currentLevelSubtitle =
    regularLevel?.spendAmountText
      ?: almostVipLevel?.spendAmountText
      ?: vipLevelHeader?.spendAmountText
      ?: maxLevelHeader?.currentLevelSubtitle

  val currentLevelImage =
    regularLevel?.currentLevelImage
      ?: almostVipLevel?.currentLevelImage
      ?: vipLevelHeader?.currentLevelImage
      ?: maxLevelHeader?.currentLevelImage

  val currentLevelProgressBar =
    regularLevel?.currentLevelProgressBar
      ?: almostVipLevel?.currentLevelProgressBar
      ?: vipLevelHeader?.currentLevelProgressBar

  val currentLevelProgressLeft =
    regularLevel?.percentageLeft
      ?: almostVipLevel?.percentageLeft
      ?: vipLevelHeader?.percentageLeft

  val vipReferralButton =
    if (regularLevel == null && almostVipLevel == null) {
      vipLevelHeader?.vipReferralBt ?: maxLevelHeader?.vipReferralBt
    } else null

}
