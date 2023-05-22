package com.asfoundation.wallet.ui.wallets

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import by.kirich1409.viewbindingdelegate.viewBinding
import com.wallet.appcoins.core.legacy_base.legacy.BaseActivity
import com.asf.wallet.R
import com.asf.wallet.databinding.RemoveWalletActivityLayoutBinding
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

@AndroidEntryPoint
class RemoveWalletActivity : com.wallet.appcoins.core.legacy_base.legacy.BaseActivity(), RemoveWalletActivityView {

  private var authenticationResultSubject: PublishSubject<Boolean>? = null

  private val binding by viewBinding(RemoveWalletActivityLayoutBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.remove_wallet_activity_layout)
    toolbar()
    authenticationResultSubject = PublishSubject.create()
    if (savedInstanceState == null) navigateToInitialRemoveWalletView()
  }

  /**
   * function hardcoded temporarily, must be changed
   * @return
   */
  override fun toolbar(): Toolbar? {
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    toolbar!!.visibility = View.VISIBLE
    if (toolbar != null) {
      setSupportActionBar(toolbar)
      toolbar.title = title
    }
    enableDisplayHomeAsUp()
    return toolbar
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == AUTHENTICATION_REQUEST_CODE) {
      if (resultCode == AuthenticationPromptActivity.RESULT_OK) {
        authenticationResultSubject?.onNext(true)
      }
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      if (binding.walletRemoveAnimation == null || binding.walletRemoveAnimation.visibility != View.VISIBLE) super.onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onBackPressed() {
    if (binding.walletRemoveAnimation == null || binding.walletRemoveAnimation.visibility != View.VISIBLE) super.onBackPressed()
  }

  override fun onDestroy() {
    authenticationResultSubject = null
    super.onDestroy()
  }

  private fun navigateToInitialRemoveWalletView() {
    supportFragmentManager.beginTransaction()
      .replace(
        R.id.fragment_container,
        RemoveWalletFragment.newInstance(walletAddress, fiatBalance)
      )
      .commit()
  }

  override fun navigateToWalletRemoveConfirmation() {
    supportFragmentManager.beginTransaction()
      .replace(
        R.id.fragment_container,
        WalletRemoveConfirmationFragment.newInstance(
          walletAddress, fiatBalance,
          appcoinsBalance, creditsBalance, ethereumBalance
        )
      )
      .addToBackStack(WalletRemoveConfirmationFragment::class.java.simpleName)
      .commit()
  }

  override fun finish() {
    super.finish()
  }

  override fun navigateToBackUp(walletAddress: String) =
    startActivity(
        com.appcoins.wallet.feature.backup.ui.BackupActivity.newIntent(this, walletAddress, false))

  override fun showRemoveWalletAnimation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    binding.walletRemoveAnimation.visibility = View.VISIBLE
    binding.removeWalletAnimation.repeatCount = 0
    binding.removeWalletAnimation.playAnimation()
  }

  override fun showAuthentication() {
    val intent = AuthenticationPromptActivity.newIntent(this)
      .apply { intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
    startActivityForResult(intent, AUTHENTICATION_REQUEST_CODE)
  }

  override fun authenticationResult(): Observable<Boolean> {
    return authenticationResultSubject!!
  }

  private val walletAddress: String by lazy {
    if (intent.extras!!.containsKey(WALLET_ADDRESS_KEY)) {
      intent.extras!!.getString(WALLET_ADDRESS_KEY)!!
    } else {
      throw IllegalArgumentException("walletAddress not found")
    }
  }

  private val fiatBalance: String by lazy {
    if (intent.extras!!.containsKey(FIAT_BALANCE_KEY)) {
      intent.extras!!.getString(FIAT_BALANCE_KEY)!!
    } else {
      throw IllegalArgumentException("fiat balance not found")
    }
  }

  private val appcoinsBalance: String by lazy {
    if (intent.extras!!.containsKey(APPC_BALANCE_KEY)) {
      intent.extras!!.getString(APPC_BALANCE_KEY)!!
    } else {
      throw IllegalArgumentException("appc balance not found")
    }
  }

  private val creditsBalance: String by lazy {
    if (intent.extras!!.containsKey(CREDITS_BALANCE_KEY)) {
      intent.extras!!.getString(CREDITS_BALANCE_KEY)!!
    } else {
      throw IllegalArgumentException("credits balance not found")
    }
  }

  private val ethereumBalance: String by lazy {
    if (intent.extras!!.containsKey(ETHEREUM_BALANCE_KEY)) {
      intent.extras!!.getString(ETHEREUM_BALANCE_KEY)!!
    } else {
      throw IllegalArgumentException("ethereum balance not found")
    }
  }

  companion object {
    private const val WALLET_ADDRESS_KEY = "wallet_address"
    private const val FIAT_BALANCE_KEY = "fiat_balance"
    private const val APPC_BALANCE_KEY = "appc_balance"
    private const val CREDITS_BALANCE_KEY = "credits_balance"
    private const val ETHEREUM_BALANCE_KEY = "ethereum_balance"
    private const val AUTHENTICATION_REQUEST_CODE = 33

    @JvmStatic
    fun newIntent(
      context: Context, walletAddress: String, totalFiatBalance: String,
      appcoinsBalance: String, creditsBalance: String,
      ethereumBalance: String
    ): Intent {
      val intent = Intent(context, RemoveWalletActivity::class.java)
      intent.putExtra(WALLET_ADDRESS_KEY, walletAddress)
      intent.putExtra(FIAT_BALANCE_KEY, totalFiatBalance)
      intent.putExtra(APPC_BALANCE_KEY, appcoinsBalance)
      intent.putExtra(CREDITS_BALANCE_KEY, creditsBalance)
      intent.putExtra(ETHEREUM_BALANCE_KEY, ethereumBalance)
      return intent
    }
  }
}
