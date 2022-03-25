package com.asfoundation.wallet.redeem_gift.bottom_sheet

import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class RedeemGiftBottomSheetNavigator @Inject constructor(val fragment: Fragment) {

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }
}