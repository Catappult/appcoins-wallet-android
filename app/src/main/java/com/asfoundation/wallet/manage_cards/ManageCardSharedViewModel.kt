package com.asfoundation.wallet.manage_cards

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManageCardSharedViewModel @Inject constructor() : ViewModel() {
  val isCardSaved = mutableStateOf(false)
  val isCardError = mutableStateOf(false)

  fun onCardSaved() {
    isCardSaved.value = true
  }

  fun onCardError() {
    isCardError.value = true
  }

  fun resetCardResult() {
    isCardSaved.value = false
    isCardError.value = false
  }
}