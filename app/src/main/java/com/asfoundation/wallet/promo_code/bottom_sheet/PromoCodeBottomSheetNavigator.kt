package com.asfoundation.wallet.promo_code.bottom_sheet

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.asf.wallet.R
import com.asfoundation.wallet.backup.skip.BackupSkipDialogFragment
import com.asfoundation.wallet.promo_code.bottom_sheet.success.PromoCodeSuccessBottomSheetFragment
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class PromoCodeBottomSheetNavigator @Inject constructor(
  val fragment: Fragment,
  val fragmentManager: FragmentManager
) {

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }

  fun navigateToSuccess(promoCode: PromoCode) {
    (fragment as BottomSheetDialogFragment).dismiss()
    val bottomSheet = PromoCodeSuccessBottomSheetFragment.newInstance(promoCode)
    bottomSheet.show(fragment.parentFragmentManager, "PromoCodeSuccess")
  }
}