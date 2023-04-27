package com.asfoundation.wallet.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.network.backend.ApiError
import com.appcoins.wallet.core.network.backend.ApiException
import com.appcoins.wallet.core.network.backend.ApiSuccess
import com.asfoundation.wallet.home.usecases.FetchTransactionsHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsListViewModel @Inject constructor(
  private val fetchTransactionsHistoryUseCase: FetchTransactionsHistoryUseCase
) : ViewModel() {

  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  init {
    fetchTransactions()
  }

  fun fetchTransactions() {
    viewModelScope.launch {
      fetchTransactionsHistoryUseCase("0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f")
        .onStart { } //TODO() Loading
        .catch { } //TODO() Logger
        .collect { result ->
          when (result) {
            is ApiSuccess -> {
              _uiState.value = UiState.Success(
                result.data
                  .map { it.toModel() }
                  .groupBy { it.date }
              )
            }

            is ApiError -> {}

            is ApiException -> {}
          }
        }
    }
  }

  sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val transactions: Map<String, List<TransactionModel>>) : UiState()
  }
}