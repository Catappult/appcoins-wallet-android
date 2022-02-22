package com.asfoundation.wallet.transfers

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import javax.inject.Inject

class TransferConfirmationNavigator @Inject constructor(private val fragmentManager: FragmentManager) {

  fun showEtherTransactionBottomSheet(transactionHash: String) {
    val bottomSheet = EtherTransactionBottomSheetFragment.newInstance(transactionHash)
    bottomSheet.show(fragmentManager, "EtherBottomSheet")
  }
}