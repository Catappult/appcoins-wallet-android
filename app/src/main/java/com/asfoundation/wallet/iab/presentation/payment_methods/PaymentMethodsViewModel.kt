package com.asfoundation.wallet.iab.presentation.payment_methods

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData
import com.asfoundation.wallet.ui.iab.PaymentMethodsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class PaymentMethodsViewModel(
  private val purchaseData: PurchaseData,
  private val purchaseInfoData: PurchaseInfoData,
  private val paymentMethodsInteractor: PaymentMethodsInteractor
) : ViewModel() {

  private val viewModelState =
    MutableStateFlow<PaymentMethodsUiState>(PaymentMethodsUiState.LoadingPaymentMethods(purchaseInfoData))

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
    // TODO
  }
}

@Composable
fun rememberPaymentMethodsViewModel(
  purchaseData: PurchaseData,
  purchaseInfoData: PurchaseInfoData,
): PaymentMethodsViewModel {
  val injectionsProvider = hiltViewModel<PaymentMethodsViewModelInjectionsProvider>()
  return viewModel<PaymentMethodsViewModel>(
    factory = object : Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PaymentMethodsViewModel(
          purchaseData = purchaseData,
          purchaseInfoData = purchaseInfoData,
          paymentMethodsInteractor = injectionsProvider.paymentMethodsInteractor
        ) as T
      }
    }
  )
}

@HiltViewModel
private class PaymentMethodsViewModelInjectionsProvider @Inject constructor(
  val paymentMethodsInteractor: PaymentMethodsInteractor
) : ViewModel()
