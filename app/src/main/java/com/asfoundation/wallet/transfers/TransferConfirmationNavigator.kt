package com.asfoundation.wallet.transfers

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R

class TransferConfirmationNavigator(private val fragmentManager: FragmentManager) {

  fun showEtherTransactionBottomSheet(transactionHash: String) {
    val bottomSheet = EtherTransactionBottomSheetFragment.newInstance(transactionHash)
    fragmentManager.beginTransaction()
        .setCustomAnimations(R.anim.fragment_slide_up, R.anim.fragment_slide_down,
            R.anim.fragment_slide_up, R.anim.fragment_slide_down)
        .add(bottomSheet, "EtherBottomSheet")
        .show(bottomSheet)
        .commit()
  }

  fun showEtherTransactionBottomSheet1(transactionHash: String) {
    val bottomSheet = EtherTransactionBottomSheetFragment.newInstance(transactionHash)
    bottomSheet.show(fragmentManager, "EtherBottomSheet")
  }
}