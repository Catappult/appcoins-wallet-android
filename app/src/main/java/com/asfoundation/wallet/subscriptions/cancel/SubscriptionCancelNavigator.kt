package com.asfoundation.wallet.subscriptions.cancel

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.subscriptions.success.SubscriptionSuccessFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class SubscriptionCancelNavigator
@Inject
constructor(private val fragmentManager: FragmentManager, private val fragment: Fragment) {

  fun showCancelSuccess() {
    val bottomSheet = SubscriptionSuccessFragment.newInstance(
      SubscriptionSuccessFragment.SubscriptionSuccess.CANCEL
    )
    dismissCurrentBottomSheet()
    bottomSheet.isCancelable = false
    bottomSheet.show(fragmentManager, "SubscriptionSuccessBottomSheet")
  }

  fun dismissCurrentBottomSheet() = (fragment as BottomSheetDialogFragment).dismiss()
}
