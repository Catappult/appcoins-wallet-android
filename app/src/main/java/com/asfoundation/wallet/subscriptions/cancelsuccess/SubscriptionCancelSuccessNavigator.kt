package com.asfoundation.wallet.subscriptions.cancelsuccess

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

class SubscriptionCancelSuccessNavigator(private val fragmentManager: FragmentManager,
                                         private val activity: FragmentActivity) {
  fun navigateBack() {
    if (fragmentManager.backStackEntryCount > 1) fragmentManager.popBackStack()
    else activity.finish()
  }
}
