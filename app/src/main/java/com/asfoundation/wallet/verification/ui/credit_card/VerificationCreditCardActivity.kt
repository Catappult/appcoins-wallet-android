package com.asfoundation.wallet.verification.ui.credit_card

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.compose.ui.platform.ComposeView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.ui.widgets.top_bar.TopBar
import com.asf.wallet.R
import com.asf.wallet.databinding.ActivityWalletVerificationBinding
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.recover.entry.RecoverEntryFragment
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class VerificationCreditCardActivity : BaseActivity(), VerificationCreditCardActivityView {

  companion object {
    const val IS_WALLET_VERIFIED = "is_wallet_verified"

    @JvmStatic
    fun newIntent(context: Context, isWalletVerified: Boolean = false) =
      Intent(context, VerificationCreditCardActivity::class.java).apply {
        putExtra(IS_WALLET_VERIFIED, isWalletVerified)
      }
  }

  @Inject
  lateinit var displayChat: DisplayChatUseCase

  @Inject
  lateinit var presenter: VerificationCreditCardActivityPresenter

  private val toolbarBackPressSubject = PublishSubject.create<String>()

  private val views by viewBinding(ActivityWalletVerificationBinding::bind)

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_wallet_verification)
    val isWalletVerified = intent.getBooleanExtra(IS_WALLET_VERIFIED, false)
    val title =
      if (isWalletVerified) R.string.verify_card_title else R.string.verification_settings_unverified_title
    setTitle("")
    toolbar()
    presenter.present(savedInstanceState)
  }

  /**
   * function hardcoded temporarily, must be changed
   * @return
   */
  fun toolbar() {
    findViewById<ComposeView>(R.id.app_bar_verify).apply {
      setContent {
        TopBar(
          isMainBar = false,
          onClickSupport = { displayChat() },
          onClickBack = { onBackPressed() },
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics)
      }
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      toolbarBackPressSubject.onNext(
        supportFragmentManager.findFragmentById(R.id.fragment_container)?.javaClass?.name ?: ""
      )
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun getCurrentFragment(): String {
    val fragments = supportFragmentManager.fragments
    return if (fragments.isNotEmpty()) fragments[0]::class.java.simpleName
    else RecoverEntryFragment::class.java.simpleName
  }

  override fun cancel() = finish()

  override fun complete() = finish()

  override fun lockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun unlockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  override fun getToolbarBackPressEvents(): Observable<String> {
    return toolbarBackPressSubject
  }

  override fun hideLoading() {
    views.progressBar.visibility = View.GONE
  }
}
