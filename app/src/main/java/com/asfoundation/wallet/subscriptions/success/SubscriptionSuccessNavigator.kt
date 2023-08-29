package com.asfoundation.wallet.subscriptions.success

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.subscriptions.list.SubscriptionListFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class SubscriptionSuccessNavigator @Inject constructor(
  private val fragmentManager: FragmentManager,
  private val fragment: Fragment
) {

  fun navigateToSubscriptionList() {
    for (i in 0 until fragmentManager.backStackEntryCount) fragmentManager.popBackStack()
    dismissCurrentBottomSheet()
    fragmentManager.beginTransaction()
      .replace(R.id.fragment_container, SubscriptionListFragment.newInstance(true))
      .addToBackStack(SubscriptionListFragment::class.java.simpleName)
      .commit()
  }

  private fun dismissCurrentBottomSheet() = (fragment as BottomSheetDialogFragment).dismiss()
}
