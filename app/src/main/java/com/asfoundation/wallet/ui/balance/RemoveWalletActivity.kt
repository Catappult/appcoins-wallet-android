package com.asfoundation.wallet.ui.balance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import kotlinx.android.synthetic.main.remove_wallet_activity_layout.*

class RemoveWalletActivity : BaseActivity(), RemoveWalletActivityView {

  private var showingAnimation: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.remove_wallet_activity_layout)
    toolbar()
    navigateToInitialRemoveWalletView()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      if (!showingAnimation) super.onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onBackPressed() {
    if (!showingAnimation) super.onBackPressed()
  }

  private fun navigateToInitialRemoveWalletView() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            RemoveWalletFragment.newInstance(walletAddress, fiatBalance))
        .commit()
  }

  override fun navigateToWalletRemoveConfirmation() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            WalletRemoveConfirmationFragment.newInstance(walletAddress, fiatBalance,
                appcoinsBalance,
                creditsBalance, ethereumBalance))
        .addToBackStack(WalletRemoveConfirmationFragment::class.java.simpleName)
        .commit()
  }

  override fun navigateToWalletList() {
    startActivity(BalanceActivity.newIntent(this, true))
  }

  override fun showRemoveWalletAnimation() {
    showingAnimation = true
    wallet_remove_animation.visibility = View.VISIBLE
    remove_wallet_animation.repeatCount = 0
    remove_wallet_animation.playAnimation()

  }

  private val walletAddress: String by lazy {
    if (intent.extras!!.containsKey(WALLET_ADDRESS_KEY)) {
      intent.extras!!.getString(WALLET_ADDRESS_KEY)
    } else {
      throw IllegalArgumentException("walletAddress not found")
    }
  }

  private val fiatBalance: String by lazy {
    if (intent.extras!!.containsKey(FIAT_BALANCE_KEY)) {
      intent.extras!!.getString(FIAT_BALANCE_KEY)
    } else {
      throw IllegalArgumentException("fiat balance not found")
    }
  }

  private val appcoinsBalance: String by lazy {
    if (intent.extras!!.containsKey(APPC_BALANCE_KEY)) {
      intent.extras!!.getString(APPC_BALANCE_KEY)
    } else {
      throw IllegalArgumentException("appc balance not found")
    }
  }

  private val creditsBalance: String by lazy {
    if (intent.extras!!.containsKey(CREDITS_BALANCE_KEY)) {
      intent.extras!!.getString(CREDITS_BALANCE_KEY)
    } else {
      throw IllegalArgumentException("credits balance not found")
    }
  }

  private val ethereumBalance: String by lazy {
    if (intent.extras!!.containsKey(ETHEREUM_BALANCE_KEY)) {
      intent.extras!!.getString(ETHEREUM_BALANCE_KEY)
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

    @JvmStatic
    fun newIntent(context: Context, walletAddress: String, totalFiatBalance: String,
                  appcoinsBalance: String, creditsBalance: String,
                  ethereumBalance: String): Intent {
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
