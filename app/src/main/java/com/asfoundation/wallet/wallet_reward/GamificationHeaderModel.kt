package com.asfoundation.wallet.wallet_reward

import android.graphics.drawable.Drawable
import com.asfoundation.wallet.promotions.model.PromotionsModel.WalletOrigin

data class GamificationHeaderModel(
  val color: Int,
  val planetImage: Drawable?,
  val spendMoreAmount: String,
  val currentSpent: Int,
  val nextLevelSpent: Int?,
  val bonusPercentage: Double,
  val isVip: Boolean,
  val isMaxVip: Boolean,
  val walletOrigin: WalletOrigin,
  val unInitialized: Boolean,
) {

  companion object {
    fun emptySkeletonLoadingState() : GamificationHeaderModel {
      return GamificationHeaderModel(
        123,
        null,
        "",
        1234,
        null,
        1.0,
        false,
        false,
        WalletOrigin.UNKNOWN,
        true
      )
    }
  }
}
