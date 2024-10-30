package com.asfoundation.wallet.iab.presentation.payment_methods

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.asfoundation.wallet.iab.payment_manager.PaymentManager
import com.asfoundation.wallet.iab.presentation.PaymentMethodData
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData
import com.asfoundation.wallet.iab.presentation.toPaymentMethodData
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentMethodsViewModel(
  private val paymentManager: PaymentManager,
  private val purchaseInfoData: PurchaseInfoData,
) : ViewModel() {

  private val viewModelState =
    MutableStateFlow<PaymentMethodsUiState>(
      PaymentMethodsUiState.LoadingPaymentMethods(purchaseInfoData)
    )

  val uiState = viewModelState
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      viewModelState.value
    )

  init {
    reload()
  }

  fun reload() {
    viewModelScope.launch {
      viewModelState.update { PaymentMethodsUiState.LoadingPaymentMethods(purchaseInfoData) }

      try {
        val paymentMethodsRequest = async {
          paymentManager.getPaymentMethods().map { it.toPaymentMethodData() }
        }

        val paymentMethods = paymentMethodsRequest.await()

        viewModelState.update {
          PaymentMethodsUiState.PaymentMethodsIdle(
            purchaseInfo = purchaseInfoData,
            paymentMethods = paymentMethods,
          )
        }

      } catch (e: Throwable) {
        viewModelState.update { PaymentMethodsUiState.PaymentMethodsError }
      }
    }
  }

  fun setSelectedPaymentMethod(paymentMethod: PaymentMethodData) {
    paymentManager.setSelectedPaymentMethod(paymentMethod.id)
  }
}

@Composable
fun rememberPaymentMethodsViewModel(
  paymentManager: PaymentManager,
  purchaseInfoData: PurchaseInfoData,
): PaymentMethodsViewModel {
  return viewModel<PaymentMethodsViewModel>(
    factory = object : Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PaymentMethodsViewModel(
          purchaseInfoData = purchaseInfoData,
          paymentManager = paymentManager,
        ) as T
      }
    }
  )
}
