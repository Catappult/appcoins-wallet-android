package com.asfoundation.wallet.router

import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.main.MainActivity

class TransactionsRouter {

  fun navigateFromSplash(context: Context, fromSupportNotification: Boolean) {
    val intent = MainActivity.newIntent(context, fromSupportNotification)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    context.startActivity(intent)
  }
}