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
  val walletOrigin: WalletOrigin
)
