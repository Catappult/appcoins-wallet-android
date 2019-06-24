package com.asfoundation.wallet.router

import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.ui.gamification.RewardsLevelActivity

class RewardsLevelRouter {
  fun open(context: Context) {
    val intent = Intent(context, RewardsLevelActivity::class.java)
    context.startActivity(intent)
  }
}