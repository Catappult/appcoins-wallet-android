package com.asfoundation.wallet.wallet_verification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import dagger.android.AndroidInjection
import javax.inject.Inject


class WalletVerificationActivity : BaseActivity(), WalletVerificationActivityView {

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

  override fun cancel() {
    finish()
  }
}
