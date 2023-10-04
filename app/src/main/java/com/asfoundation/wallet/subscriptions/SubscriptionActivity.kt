package com.asfoundation.wallet.subscriptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.compose.ui.platform.ComposeView
import com.appcoins.wallet.ui.widgets.TopBar
import com.asf.wallet.R
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.subscriptions.list.SubscriptionListFragment
import com.asfoundation.wallet.subscriptions.success.SubscriptionSuccessFragment
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionActivity : BaseActivity() {

  @Inject
  lateinit var displayChat: DisplayChatUseCase

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_subscriptions)
    toolbar()

    if (savedInstanceState == null) showSubscriptionList()
  }

  /**
   * function hardcoded temporarily, must be changed
   * @return
   */
  fun toolbar() {
    findViewById<ComposeView>(R.id.app_bar).apply {
      setContent {
        TopBar(isMainBar = false, onClickSupport = { displayChat() })
      }
    }
  }

  private fun showSubscriptionList() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SubscriptionListFragment.newInstance())
        .addToBackStack(SubscriptionListFragment::class.java.simpleName)
        .commit()
  }

  private fun endCancelSubscription() = close(true)

  override fun onBackPressed() = close()

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      if (supportFragmentManager.backStackEntryCount > 1) {
        if (supportFragmentManager.fragments.last() is SubscriptionSuccessFragment) {
          endCancelSubscription()
        } else {
          close()
        }
      } else {
        close()
      }
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  private fun close(navigateToListFragment: Boolean = false) {
    if (supportFragmentManager.backStackEntryCount == 1) {
      finish()
    } else if (supportFragmentManager.backStackEntryCount > 1) {
      if (navigateToListFragment) {
        supportFragmentManager.popBackStack(SubscriptionListFragment::class.java.simpleName, 0)
      } else {
        supportFragmentManager.popBackStack()
      }
    }
  }

  companion object {

    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, SubscriptionActivity::class.java)
    }
  }
}