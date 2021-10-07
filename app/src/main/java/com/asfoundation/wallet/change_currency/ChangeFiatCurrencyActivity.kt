package com.asfoundation.wallet.change_currency

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity

class ChangeFiatCurrencyActivity : BaseActivity() {
  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, ChangeFiatCurrencyActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_change_fiat_currency)
    setTitle(R.string.change_currency_title)
    toolbar()
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
          .replace(R.id.fragment_container, ChangeFiatCurrencyFragment.newInstance())
          .commit()
    }
  }
}