package com.asfoundation.wallet.router

import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.ui.TransactionsActivity

class TransactionsRouter {

  fun open(context: Context, isClearStack: Boolean) {
    // TODO: Later we should review how this should behave for each case with MainActivity
    val intent = Intent(context, TransactionsActivity::class.java)
    if (isClearStack) {
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    } else {
      intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    context.startActivity(intent)
  }

  fun navigateFromSplash(context: Context, fromSupportNotification: Boolean) {
    val intent = MainActivity.newIntent(context, fromSupportNotification)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    context.startActivity(intent)
  }
}