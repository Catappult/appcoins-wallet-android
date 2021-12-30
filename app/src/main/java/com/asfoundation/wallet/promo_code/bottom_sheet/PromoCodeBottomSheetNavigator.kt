package com.asfoundation.wallet.promo_code.bottom_sheet

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PromoCodeBottomSheetNavigator(val fragment: BottomSheetDialogFragment) {

  fun navigateBack() {
    fragment.dismiss()
  }
}