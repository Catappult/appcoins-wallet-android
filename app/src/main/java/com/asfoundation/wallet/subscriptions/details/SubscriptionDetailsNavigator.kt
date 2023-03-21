package com.asfoundation.wallet.subscriptions.details

import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.subscriptions.cancel.SubscriptionCancelFragment
import com.asfoundation.wallet.subscriptions.success.SubscriptionSuccessFragment
import com.appcoins.wallet.core.utils.android_common.SharedElementTransition
import javax.inject.Inject

class SubscriptionDetailsNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

  fun showCancelSubscription(subscriptionItem: SubscriptionItem, sharedElement: View) {
    val transitionName = ViewCompat.getTransitionName(sharedElement) ?: ""
    val fragment = SubscriptionCancelFragment.newInstance(subscriptionItem, transitionName)
        .apply {
          sharedElementEnterTransition = SharedElementTransition()
          sharedElementReturnTransition = SharedElementTransition()
          postponeEnterTransition()
        }
    fragmentManager.beginTransaction()
        .setReorderingAllowed(true)
        .replace(R.id.fragment_container, fragment)
        .addSharedElement(sharedElement, transitionName)
        .addToBackStack(SubscriptionCancelFragment::class.java.simpleName)
        .commit()
  }

  fun showRenewSuccess() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SubscriptionSuccessFragment.newInstance(
            SubscriptionSuccessFragment.SubscriptionSuccess.RENEW))
        .addToBackStack(SubscriptionSuccessFragment::class.java.simpleName)
        .commit()
  }
}
