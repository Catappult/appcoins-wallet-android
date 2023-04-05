package com.asfoundation.wallet.wallet_blocked

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.databinding.LayoutWalletBlockedBinding
import com.asfoundation.wallet.ui.BaseActivity
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

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

  private var _binding: LayoutWalletBlockedBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private val blocked_email get() = binding.blockedEmail
  private val dismiss_button get() = binding.dismissButton

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    _binding = LayoutWalletBlockedBinding.inflate(layoutInflater)
    setContentView(binding.root)

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