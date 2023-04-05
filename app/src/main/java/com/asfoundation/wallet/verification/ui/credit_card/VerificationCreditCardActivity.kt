package com.asfoundation.wallet.verification.ui.credit_card

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.asf.wallet.R
import com.asf.wallet.databinding.ActivityWalletVerificationBinding
import com.asfoundation.wallet.recover.entry.RecoverEntryFragment
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.verification.ui.credit_card.code.VerificationCodeFragment
import com.asfoundation.wallet.verification.ui.credit_card.error.VerificationErrorFragment
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
  lateinit var presenter: VerificationCreditCardActivityPresenter

  private val toolbarBackPressSubject = PublishSubject.create<String>()

  private var _binding: ActivityWalletVerificationBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private val progress_bar get() = binding.progressBar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    _binding = ActivityWalletVerificationBinding.inflate(layoutInflater)
    setContentView(binding.root)
    val isWalletVerified = intent.getBooleanExtra(IS_WALLET_VERIFIED, false)
    val title =
        if (isWalletVerified) R.string.verify_card_title else R.string.verification_settings_unverified_title
    setTitle(title)
    toolbar()
    presenter.present(savedInstanceState)
  }

  override fun onBackPressed() {
    val fragmentName =
        supportFragmentManager.findFragmentById(R.id.fragment_container)?.javaClass?.name ?: ""
    if (fragmentName == VerificationErrorFragment::class.java.name ||
        fragmentName == VerificationCodeFragment::class.java.name) {
      toolbarBackPressSubject.onNext(fragmentName)
    } else {
      super.onBackPressed()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      toolbarBackPressSubject.onNext(
          supportFragmentManager.findFragmentById(R.id.fragment_container)?.javaClass?.name ?: "")
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
    progress_bar.visibility = View.GONE
  }
}
