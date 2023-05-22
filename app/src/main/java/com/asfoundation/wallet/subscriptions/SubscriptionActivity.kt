package com.asfoundation.wallet.subscriptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.wallet.appcoins.core.legacy_base.legacy.BaseActivity
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.list.SubscriptionListFragment
import com.asfoundation.wallet.subscriptions.success.SubscriptionSuccessFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionActivity : com.wallet.appcoins.core.legacy_base.legacy.BaseActivity() {

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
  override fun toolbar(): Toolbar {
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    toolbar!!.visibility = View.VISIBLE
    if (toolbar != null) {
      setSupportActionBar(toolbar)
      toolbar.title = title
    }
    enableDisplayHomeAsUp()
    return toolbar
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