package com.asfoundation.wallet.wallet_verification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.navigator.ActivityNavigator
import dagger.android.AndroidInjection
import javax.inject.Inject


class WalletVerificationActivity : ActivityNavigator(), WalletVerificationActivityView {

  companion object {

    @JvmStatic
    fun newIntent(context: Context) = Intent(context, WalletVerificationActivity::class.java)
  }

  @Inject
  lateinit var presenter: WalletVerificationActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_wallet_verification)
    toolbar()
    presenter.present(savedInstanceState)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      presenter.sendBackEvent()
      super.onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}
