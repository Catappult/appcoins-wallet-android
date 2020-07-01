package com.asfoundation.wallet.router

import android.content.Context
import com.asfoundation.wallet.ui.gamification.RewardsLevelActivity

class RewardsLevelRouter {
  fun open(context: Context, legacy: Boolean, bonus: Double) =
      context.startActivity(RewardsLevelActivity.newIntent(context, legacy, bonus))
}