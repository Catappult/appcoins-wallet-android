package com.asfoundation.wallet.wallet_blocked

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.layout_wallet_blocked.*

@AndroidEntryPoint
class WalletBlockedActivity : BaseActivity(),
    WalletBlockedView {

  private lateinit var presenter: WalletBlockedPresenter

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, WalletBlockedActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.layout_wallet_blocked)

    presenter =
        WalletBlockedPresenter(this,
            CompositeDisposable(), AndroidSchedulers.mainThread())
    presenter.present()
  }

  override fun onResume() {
    super.onResume()
    sendPageViewEvent()
  }

  override fun getDismissCLicks(): Observable<Any> {
    return RxView.clicks(dismiss_button)
  }

  override fun getEmailClicks(): Observable<Any> {
    return RxView.clicks(blocked_email)
  }

  override fun openEmail() {
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        .apply {
          type = "message/rfc822"
          putExtra(Intent.EXTRA_SUBJECT, "Blocked wallet")
          putExtra(Intent.EXTRA_EMAIL, arrayOf("info@appcoins.io"))
        }
    startActivity(Intent.createChooser(intent, "Select email application."))
  }

  override fun dismiss() {
    closeSuccess()
  }

  override fun onBackPressed() {
    closeSuccess()
    super.onBackPressed()
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  private fun closeSuccess() {
    setResult(Activity.RESULT_OK, Intent())
    finish()
  }

}