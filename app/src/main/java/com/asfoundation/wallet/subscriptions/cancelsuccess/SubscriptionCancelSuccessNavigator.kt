package com.asfoundation.wallet.subscriptions.cancelsuccess

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.list.SubscriptionListFragment

class SubscriptionCancelSuccessNavigator(private val fragmentManager: FragmentManager) {

  fun navigateToSubscriptionList() {
    for (i in 0 until fragmentManager.backStackEntryCount) fragmentManager.popBackStack()
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SubscriptionListFragment.newInstance(true))
        .addToBackStack(SubscriptionListFragment::class.java.simpleName)
        .commit()
  }
}
