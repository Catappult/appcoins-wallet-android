package com.asfoundation.wallet.withdraw.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity

class WithdrawActivity: BaseActivity() {
  companion object{
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, WithdrawActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.withdraw_activity)
  }
}