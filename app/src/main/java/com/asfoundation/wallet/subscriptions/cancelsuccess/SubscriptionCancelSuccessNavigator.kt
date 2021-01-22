package com.asfoundation.wallet.subscriptions.cancelsuccess

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.list.SubscriptionListFragment

class SubscriptionCancelSuccessNavigator(private val fragmentManager: FragmentManager) {

  fun navigateToSubscriptionList() {
    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SubscriptionListFragment.newInstance())
        .addToBackStack(SubscriptionListFragment::class.java.simpleName)
        .commit()
  }
}
