package com.asfoundation.wallet.transfers

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R

class TransferConfirmationNavigator(private val fragmentManager: FragmentManager) {

  fun showEtherTransactionBottomSheet(transactionHash: String) {
    val bottomSheet = EtherTransactionBottomSheetFragment.newInstance(transactionHash)
    bottomSheet.show(fragmentManager, "EtherBottomSheet")
  }
}