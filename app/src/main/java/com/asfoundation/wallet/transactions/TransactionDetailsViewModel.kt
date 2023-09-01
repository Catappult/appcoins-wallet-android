package com.asfoundation.wallet.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.base.call_adapter.ApiException
import com.appcoins.wallet.core.network.base.call_adapter.ApiFailure
import com.appcoins.wallet.core.network.base.call_adapter.ApiSuccess
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.home.usecases.GetInvoiceByIdUseCase
import com.asfoundation.wallet.transactions.TransactionDetailsViewModel.InvoiceState.ApiError
import com.asfoundation.wallet.transactions.TransactionDetailsViewModel.InvoiceState.Idle
import com.asfoundation.wallet.transactions.TransactionDetailsViewModel.InvoiceState.InvoiceSuccess
import com.asfoundation.wallet.transactions.TransactionDetailsViewModel.UiState.Loading
import com.asfoundation.wallet.transactions.TransactionDetailsViewModel.UiState.TransactionSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailsViewModel
@Inject
constructor(
  private val displayChatUseCase: DisplayChatUseCase,
  private val ewtAuthenticatorService: EwtAuthenticatorService,
  private val getInvoiceByIdUseCase: GetInvoiceByIdUseCase,
  private val logger: Logger
) : ViewModel() {
  private val tag = TransactionDetailsViewModel::class.java.name

  private val _uiState = MutableStateFlow<UiState>(Loading)
  var uiState: StateFlow<UiState> = _uiState

  private val _invoiceState = MutableStateFlow<InvoiceState>(Idle)
  var invoiceState: StateFlow<InvoiceState> = _invoiceState

  fun displayChat() {
    displayChatUseCase()
  }

  fun updateTransaction(transaction: TransactionModel?) {
    _uiState.value = if (transaction != null) TransactionSuccess(transaction) else Loading
  }

  fun downloadInvoice(invoiceId: String) {
    ewtAuthenticatorService
      .getEwtAuthentication()
      .doOnSuccess { ewt ->
        viewModelScope.launch {
          getInvoiceByIdUseCase(invoiceId, ewt)
            .catch { logger.log(tag, it) }
            .collect { result ->
              when (result) {
                is ApiSuccess -> {
                  _invoiceState.value = InvoiceSuccess(result.data.url, invoiceId)
                }

                is ApiException -> {
                  _invoiceState.value = ApiError
                  logger.log(tag, result.e)
                }

                is ApiFailure -> {
                  _invoiceState.value = ApiError
                  logger.log(tag, "${result.code}  ${result.message}")
                }
              }
            }
        }
      }
      .subscribe()
  }

  sealed class UiState {
    object Loading : UiState()
    data class TransactionSuccess(val transaction: TransactionModel) : UiState()
  }

  sealed class InvoiceState {
    object Idle : InvoiceState()
    object ApiError : InvoiceState()
    data class InvoiceSuccess(val url: String, val invoiceId: String) : InvoiceState()
  }
}
