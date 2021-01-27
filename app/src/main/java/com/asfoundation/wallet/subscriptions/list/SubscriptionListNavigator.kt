package com.asfoundation.wallet.subscriptions.list

import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.subscriptions.details.SubscriptionDetailsFragment
import com.asfoundation.wallet.util.SharedElementTransition

class SubscriptionListNavigator(private val fragmentManager: FragmentManager) {

  fun showSubscriptionDetails(subscriptionItem: SubscriptionItem, sharedElement: View) {
    val fragment = SubscriptionDetailsFragment.newInstance(subscriptionItem)
        .apply {
          sharedElementEnterTransition = SharedElementTransition()
          postponeEnterTransition()
        }
    fragmentManager.beginTransaction()
        .setReorderingAllowed(true)
        .replace(R.id.fragment_container, fragment)
        .addSharedElement(sharedElement, ViewCompat.getTransitionName(sharedElement) ?: "")
        .addToBackStack(SubscriptionDetailsFragment::class.java.simpleName)
        .commit()
  }
}
