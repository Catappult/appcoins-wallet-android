package com.asfoundation.wallet.subscriptions.list

import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.subscriptions.details.SubscriptionDetailsFragment
import com.appcoins.wallet.core.utils.common.SharedElementTransition
import javax.inject.Inject

class SubscriptionListNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

  fun showSubscriptionDetails(subscriptionItem: SubscriptionItem, sharedElement: View) {
    val transitionName = ViewCompat.getTransitionName(sharedElement) ?: ""
    val fragment = SubscriptionDetailsFragment.newInstance(subscriptionItem, transitionName)
        .apply {
          sharedElementEnterTransition = SharedElementTransition()
          sharedElementReturnTransition = SharedElementTransition()
          postponeEnterTransition()
        }
    fragmentManager.beginTransaction()
        .setReorderingAllowed(true)
        .replace(R.id.fragment_container, fragment)
        .addSharedElement(sharedElement, transitionName)
        .addToBackStack(SubscriptionDetailsFragment::class.java.simpleName)
        .commit()
  }
}
