package com.asfoundation.wallet.subscriptions.details

import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.subscriptions.SubscriptionItem
import com.asfoundation.wallet.subscriptions.cancel.SubscriptionCancelFragment
import com.asfoundation.wallet.subscriptions.success.SubscriptionSuccessFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class SubscriptionDetailsNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

  fun showCancelSubscription(subscriptionItem: SubscriptionItem, sharedElement: View) {
    val transitionName = ViewCompat.getTransitionName(sharedElement) ?: ""
    val bottomSheet = SubscriptionCancelFragment.newInstance(subscriptionItem, transitionName)
        as BottomSheetDialogFragment
    fragmentManager
    bottomSheet.show(fragmentManager, "SubscriptionCancelBottomSheet")
  }

  fun showRenewSuccess() {
    val bottomSheet = SubscriptionSuccessFragment.newInstance(
      SubscriptionSuccessFragment.SubscriptionSuccess.RENEW
    ) as BottomSheetDialogFragment
    bottomSheet.isCancelable = false
    bottomSheet.show(fragmentManager, "SubscriptionSuccessBottomSheet")
  }
}
