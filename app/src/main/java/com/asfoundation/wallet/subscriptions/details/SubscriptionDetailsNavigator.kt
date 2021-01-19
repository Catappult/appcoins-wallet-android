package com.asfoundation.wallet.subscriptions.details

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.subscriptions.cancel.SubscriptionCancelFragment

class SubscriptionDetailsNavigator(private val fragmentManager: FragmentManager) {

  fun showCancelSubscription(subscriptionItem: SubscriptionItem) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SubscriptionCancelFragment.newInstance(subscriptionItem))
        .addToBackStack(SubscriptionCancelFragment::class.java.simpleName)
        .commit()
  }
}
