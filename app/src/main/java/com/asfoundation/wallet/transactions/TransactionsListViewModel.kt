package com.asfoundation.wallet.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.network.base.call_adapter.ApiException
import com.appcoins.wallet.core.network.base.call_adapter.ApiFailure
import com.appcoins.wallet.core.network.base.call_adapter.ApiSuccess
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.home.usecases.FetchTransactionsHistoryUseCase
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import com.github.michaelbull.result.unwrap
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

@HiltViewModel
class TransactionsListViewModel @Inject constructor(
  private val fetchTransactionsHistoryUseCase: FetchTransactionsHistoryUseCase,
  private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase,
  private val getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
  private val logger: Logger
) : ViewModel() {
  private lateinit var defaultCurrency: String
  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  companion object {
    private val TAG = TransactionsListViewModel::class.java.name
  }

  init {
    observeWalletData()
  }

  fun displayChat() {
    displayChatUseCase()
  }

  private fun observeWalletData() {
    Observable.combineLatest(
      rxSingle { getSelectedCurrencyUseCase(false) }.toObservable(), observeDefaultWalletUseCase()
    ) { selectedCurrency, wallet ->
      defaultCurrency = selectedCurrency.unwrap()
      fetchTransactions(wallet.address, defaultCurrency)
    }.subscribe()
  }

  private fun fetchTransactions(walletAddress: String, selectedCurrency: String) {
    viewModelScope.launch {
      fetchTransactionsHistoryUseCase(wallet = walletAddress, currency = selectedCurrency)
        .onStart { _uiState.value = UiState.Loading }
        .catch { logger.log(TAG, it) }
        .collect { result ->
          when (result) {
            is ApiSuccess -> {
              _uiState.value = UiState.Success(
                result.data
                  .map { it.toModel(defaultCurrency) }
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