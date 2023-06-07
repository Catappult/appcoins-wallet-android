package com.asfoundation.wallet.promo_code.bottom_sheet

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.asfoundation.wallet.promo_code.bottom_sheet.success.PromoCodeSuccessBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class PromoCodeBottomSheetNavigator @Inject constructor(
  val fragment: Fragment,
  val fragmentManager: FragmentManager
) {

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }

  fun navigateToSuccess(promoCode: com.appcoins.wallet.feature.promocode.data.repository.PromoCode) {
    (fragment as BottomSheetDialogFragment).dismiss()
    val bottomSheet = PromoCodeSuccessBottomSheetFragment.newInstance(promoCode)
    bottomSheet.show(fragment.parentFragmentManager, "PromoCodeSuccess")
  }
}