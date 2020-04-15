package com.asfoundation.wallet.router

import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.topup.TopUpActivity

class TopUpRouter {

  fun open(context: Context) {
    val intent = TopUpActivity.newIntent(context)
        .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
    context.startActivity(intent)
  }
}
