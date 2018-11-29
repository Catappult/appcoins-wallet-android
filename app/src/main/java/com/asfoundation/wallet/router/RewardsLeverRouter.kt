package com.asfoundation.wallet.router

import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.ui.rewards.RewardsLevelActivity

class RewardsLeverRouter {
  fun open(context: Context) {
    val intent = Intent(context, RewardsLevelActivity::class.java)
    context.startActivity(intent)
  }
}