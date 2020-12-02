package com.asfoundation.wallet.verification

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.restore.intro.RestoreWalletFragment
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


  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      super.onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun getCurrentFragment(): String {
    val fragments = supportFragmentManager.fragments
    return if (fragments.isNotEmpty()) fragments[0]::class.java.simpleName
    else RestoreWalletFragment::class.java.simpleName
  }

  override fun cancel() {
    finish()
  }

  override fun complete() {
    finish()
  }

  override fun lockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun unlockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }
}
