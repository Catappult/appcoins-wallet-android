package com.asfoundation.wallet.transfers

import androidx.fragment.app.FragmentManager
import javax.inject.Inject

class TransferConfirmationNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

  fun showEtherTransactionBottomSheet(transactionHash: String) {
    val bottomSheet = EtherTransactionBottomSheetFragment.newInstance(transactionHash)
    bottomSheet.show(fragmentManager, "EtherBottomSheet")
  }
}