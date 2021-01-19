package com.asfoundation.wallet.subscriptions.list

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.subscriptions.details.SubscriptionDetailsFragment

class SubscriptionListNavigator(private val fragmentManager: FragmentManager) {

  fun showSubscriptionDetails(subscriptionItem: SubscriptionItem) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SubscriptionDetailsFragment.newInstance(subscriptionItem))
        .addToBackStack(SubscriptionDetailsFragment::class.java.simpleName)
        .commit()
  }
}
