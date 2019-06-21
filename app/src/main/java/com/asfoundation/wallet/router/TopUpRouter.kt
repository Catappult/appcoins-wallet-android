package com.asfoundation.wallet.router

import android.content.Context
import com.asfoundation.wallet.topup.TopUpActivity

class TopUpRouter {

  fun open(context: Context) {
    val intent = TopUpActivity.newIntent(context)
    context.startActivity(intent)
  }
}
