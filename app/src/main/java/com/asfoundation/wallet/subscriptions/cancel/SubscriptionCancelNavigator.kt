package com.asfoundation.wallet.subscriptions.cancel

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.cancelsuccess.SubscriptionCancelSuccessFragment

class SubscriptionCancelNavigator(private val fragmentManager: FragmentManager,
                                  private val activity: FragmentActivity) {

  fun showCancelSuccess() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SubscriptionCancelSuccessFragment.newInstance())
        .addToBackStack(SubscriptionCancelSuccessFragment::class.java.simpleName)
        .commit()
  }

  fun navigateBack() {
    if (fragmentManager.backStackEntryCount > 1) fragmentManager.popBackStack()
    else activity.finish()
  }
}
