package com.asfoundation.wallet.manage_wallets.bottom_sheet

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManageWalletSharedViewModel @Inject constructor() : ViewModel() {
  val dialogDismissed = mutableStateOf(0)

  fun onBottomSheetDismissed() {
    // increments a value for the fragment to be notified
    dialogDismissed.value = dialogDismissed.value + 1
  }
}