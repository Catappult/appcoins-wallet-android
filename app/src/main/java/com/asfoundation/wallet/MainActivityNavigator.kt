package com.asfoundation.wallet

import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.topup.TopUpActivity

class MainActivityNavigator(val context: Context) {

  fun navigateToTopUp() {
    val intent = TopUpActivity.newIntent(context)
        .apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
    context.startActivity(intent)
  }
}