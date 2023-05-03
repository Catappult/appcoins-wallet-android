package com.asfoundation.wallet.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.network.backend.ApiFailure
import com.appcoins.wallet.core.network.backend.ApiException
import com.appcoins.wallet.core.network.backend.ApiSuccess
import com.appcoins.wallet.core.utils.jvm_common.Logger
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
  private val fetchTransactionsHistoryUseCase: FetchTransactionsHistoryUseCase,
  private val logger: Logger
) : ViewModel() {
  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  companion object {
    private val TAG = TransactionsListViewModel::class.java.name
  }

  init {
    fetchTransactions()
  }

  private fun fetchTransactions() {
    viewModelScope.launch {
      fetchTransactionsHistoryUseCase("")
        .onStart { _uiState.value = UiState.Loading }
        .catch { logger.log(TAG, it) }
        .collect { result ->
          when (result) {
            is ApiSuccess -> {
              _uiState.value = UiState.Success(
                result.data
                  .map { it.toModel() }
                  .groupBy { it.date }
              )
            }

            is ApiFailure -> {}

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