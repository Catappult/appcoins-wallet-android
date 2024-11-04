package com.asfoundation.wallet.wallet_reward

import android.graphics.drawable.Drawable
import com.asfoundation.wallet.promotions.model.PartnerPerk
import com.asfoundation.wallet.promotions.model.PromotionsModel.WalletOrigin

data class GamificationHeaderModel(
  val color: Int,
  val planetImage: Drawable?,
  val spendMoreAmount: String,
  val currentSpent: Int,
  val nextLevelSpent: Int?,
  val bonusPercentage: Double,
  val partnerPerk: PartnerPerk?,
  val isVip: Boolean,
  val isMaxVip: Boolean,
  val walletOrigin: WalletOrigin,
  val uninitialized: Boolean,
) {

  companion object {
    fun emptySkeletonLoadingState(): GamificationHeaderModel {
      return GamificationHeaderModel(
        color = 123,
        planetImage = null,
        spendMoreAmount = "",
        currentSpent = 1234,
        nextLevelSpent = null,
        bonusPercentage = 1.0,
        partnerPerk = null,
        isVip = false,
        isMaxVip = false,
        walletOrigin = WalletOrigin.UNKNOWN,
        uninitialized = true
      )
    }
  }
}
