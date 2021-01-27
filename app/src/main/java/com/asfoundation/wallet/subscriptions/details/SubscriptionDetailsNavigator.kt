package com.asfoundation.wallet.subscriptions.details

import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.subscriptions.cancel.SubscriptionCancelFragment
import com.asfoundation.wallet.util.SharedElementTransition

class SubscriptionDetailsNavigator(private val fragmentManager: FragmentManager) {

  fun showCancelSubscription(subscriptionItem: SubscriptionItem, sharedElement: View) {
    val fragment = SubscriptionCancelFragment.newInstance(subscriptionItem)
        .apply {
          sharedElementEnterTransition = SharedElementTransition()
          sharedElementReturnTransition = SharedElementTransition()
          postponeEnterTransition()
        }
    fragmentManager.beginTransaction()
        .setReorderingAllowed(true)
        .replace(R.id.fragment_container, fragment)
        .addSharedElement(sharedElement, ViewCompat.getTransitionName(sharedElement) ?: "")
        .addToBackStack(SubscriptionCancelFragment::class.java.simpleName)
        .commit()
  }
}
