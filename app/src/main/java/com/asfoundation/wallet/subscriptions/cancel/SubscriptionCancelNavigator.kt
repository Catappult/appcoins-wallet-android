package com.asfoundation.wallet.subscriptions.cancel

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.success.SubscriptionSuccessFragment
import javax.inject.Inject

class SubscriptionCancelNavigator @Inject constructor(private val fragmentManager: FragmentManager,
                                  private val fragment: Fragment) {

  fun showCancelSuccess() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, SubscriptionSuccessFragment.newInstance(
            SubscriptionSuccessFragment.SubscriptionSuccess.CANCEL))
        .addToBackStack(SubscriptionSuccessFragment::class.java.simpleName)
        .commit()
  }

  fun navigateBack() {
    if (fragmentManager.backStackEntryCount > 1) fragmentManager.popBackStack()
    else fragment.requireActivity().finish()
  }
}
