package com.asfoundation.wallet.transactions

import androidx.lifecycle.ViewModel
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.transactions.TransactionDetailsViewModel.UiState.Loading
import com.asfoundation.wallet.transactions.TransactionDetailsViewModel.UiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TransactionDetailsViewModel
@Inject
constructor(
  private val displayChatUseCase: DisplayChatUseCase,
) : ViewModel() {
  private val _uiState = MutableStateFlow<UiState>(Loading)
  var uiState: StateFlow<UiState> = _uiState

  fun displayChat() {
    displayChatUseCase()
  }

  fun updateTransaction(transaction: TransactionModel?) {
    _uiState.value = if (transaction != null) Success(transaction) else Loading
  }

  sealed class UiState {
    object Loading : UiState()
    data class Success(val transaction: TransactionModel) : UiState()
  }
}
