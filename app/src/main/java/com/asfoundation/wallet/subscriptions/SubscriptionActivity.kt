package com.asfoundation.wallet.subscriptions

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.TransactionsActivity

class SubscriptionActivity : BaseActivity(), SubscriptionView {

  private lateinit var presenter: SubscriptionPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_subscriptions)
    toolbar()

    presenter = SubscriptionPresenter(this)
  }

  override fun onResume() {
    super.onResume()
    presenter.present(actionMode, appPackage)
  }

  override fun showSubscriptionList() {
    toolbar().title = getString(R.string.subscriptions_settings_title)
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SubscriptionListFragment.newInstance())
        .addToBackStack(SubscriptionListFragment::class.java.simpleName)
        .commit()
  }

  override fun showSubscriptionDetails(packageName: String) {
    toolbar().title = getString(R.string.subscriptions_title)
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SubscriptionDetailsFragment.newInstance(packageName))
        .addToBackStack(SubscriptionDetailsFragment::class.java.simpleName)
        .commit()
  }

  override fun showCancelSubscription(packageName: String) {
    toolbar().title = getString(R.string.subscriptions_title)
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SubscriptionCancelFragment.newInstance(packageName))
        .addToBackStack(SubscriptionCancelFragment::class.java.simpleName)
        .commit()
  }

  override fun showCancelSuccess() {
    toolbar().title = getString(R.string.subscriptions_title)
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SubscriptionCancelSuccessFragment.newInstance())
        .addToBackStack(SubscriptionCancelSuccessFragment::class.java.simpleName)
        .commit()
  }

  override fun endCancelSubscription() {
    if (sourceTransactions) {
      val intent = TransactionsActivity.newIntent(this)
          .apply { flags = FLAG_ACTIVITY_CLEAR_TOP }
      startActivity(intent)
    } else {
      close(true)
    }
  }

  override fun navigateBack() = close()

  override fun onBackPressed() = close()

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    if (item?.itemId == android.R.id.home) {
      if (supportFragmentManager.backStackEntryCount > 1) {
        if (supportFragmentManager.fragments.last() is SubscriptionCancelSuccessFragment) {
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

  private val actionMode: Int by lazy {
    intent.getIntExtra(ACTION, 0)
  }

  private val appPackage: String? by lazy {
    intent.getStringExtra(PACKAGE_NAME)
  }

  private val sourceTransactions: Boolean by lazy {
    intent.getBooleanExtra(SOURCE, false)
  }

  companion object {

    const val ACTION = "action"
    const val PACKAGE_NAME = "package_name"
    const val SOURCE = "source_transactions"
    const val ACTION_LIST = 0
    const val ACTION_CANCEL = 1
    const val ACTION_DETAILS = 2

    @JvmStatic
    fun newIntent(context: Context, action: Int): Intent {
      return Intent(context, SubscriptionActivity::class.java)
          .apply {
            putExtra(ACTION, action)
            putExtra(SOURCE, false)
          }
    }

    @JvmStatic
    fun newIntent(context: Context, action: Int, appPackage: String): Intent {
      return Intent(context, SubscriptionActivity::class.java)
          .apply {
            putExtra(ACTION, action)
            putExtra(PACKAGE_NAME, appPackage)
            putExtra(SOURCE, true)
          }
    }
  }

}