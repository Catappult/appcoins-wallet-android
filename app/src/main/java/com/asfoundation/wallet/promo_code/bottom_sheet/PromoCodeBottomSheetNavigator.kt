package com.asfoundation.wallet.promo_code.bottom_sheet

class PromoCodeBottomSheetNavigator(val fragment: PromoCodeBottomSheetFragment) {

  fun navigateBack() {
    fragment.dismiss()
  }
}