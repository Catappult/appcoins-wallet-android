package com.asfoundation.wallet.wallet_reward

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RewardSharedViewModel @Inject constructor() : ViewModel() {
  val dialogDismissed = mutableStateOf(0)

  fun onBottomSheetDismissed() {
    // increments a value for the fragment to be notified
    dialogDismissed.value = dialogDismissed.value + 1
  }
}