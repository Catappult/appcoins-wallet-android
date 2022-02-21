package com.asfoundation.wallet.promo_code.bottom_sheet

import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class PromoCodeBottomSheetNavigator @Inject constructor(val fragment: Fragment) {

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }
}