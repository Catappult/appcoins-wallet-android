package com.asfoundation.wallet.router

import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.ui.balance.BalanceActivity

class BalanceRouter {

  fun open(context: Context) {
    val intent = Intent(context, BalanceActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    context.startActivity(intent)
  }
}
