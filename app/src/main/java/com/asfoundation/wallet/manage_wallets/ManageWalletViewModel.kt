package com.asfoundation.wallet.manage_wallets

import androidx.lifecycle.ViewModel
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ManageWalletViewModel
@Inject
constructor(private val displayChatUseCase: DisplayChatUseCase) : ViewModel() {

  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  fun displayChat() {
    displayChatUseCase()
  }

  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
  }
}
