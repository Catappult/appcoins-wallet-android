package com.asfoundation.wallet.eskills.withdraw

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class WithdrawBottomSheetNavigator @Inject constructor(
  val fragment: Fragment,
  val fragmentManager: FragmentManager
) {

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }

  fun navigateToSuccess(amount: String) {
    (fragment as BottomSheetDialogFragment).dismiss()
    val bottomSheet = WithdrawSuccessBottomSheetFragment.newInstance(amount)
    bottomSheet.show(fragment.parentFragmentManager, "WithdrawSuccess")
  }
}
