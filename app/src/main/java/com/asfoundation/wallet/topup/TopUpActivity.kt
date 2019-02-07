package com.asfoundation.wallet.topup

import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.ui.BaseActivity

class TopUpActivity : BaseActivity() {
  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, TopUpActivity::class.java)
    }
  }
}
