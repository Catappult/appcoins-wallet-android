package com.asfoundation.wallet.iab.presentation.payment_methods

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.domain.use_case.GetBalanceUseCase
import com.asfoundation.wallet.iab.domain.use_case.GetPaymentMethodsUseCase
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData
import com.asfoundation.wallet.iab.presentation.toPaymentMethodData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class PaymentMethodsViewModel(
  private val purchaseData: PurchaseData,
  private val purchaseInfoData: PurchaseInfoData,
  private val getPaymentMethodsUseCase: GetPaymentMethodsUseCase,
  private val getBalanceUseCase: GetBalanceUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils
) : ViewModel() {

  private val viewModelState =
    MutableStateFlow<PaymentMethodsUiState>(
      PaymentMethodsUiState.LoadingPaymentMethods(
        purchaseInfoData
      )
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
        val balanceRequest = async { getBalanceUseCase() }
        val paymentMethodsRequest = async {
          getPaymentMethodsUseCase(
            value = purchaseData.purchaseValue,
            currency = purchaseData.currency,
            transactionType = purchaseData.type,
            packageName = purchaseData.domain,
            entityOemId = purchaseData.oemId,
          ).map { it.toPaymentMethodData() }
        }

        val balance = balanceRequest.await()
        val paymentMethods = paymentMethodsRequest.await()

        viewModelState.update {
          PaymentMethodsUiState.PaymentMethodsIdle(
            purchaseInfo = purchaseInfoData,
            paymentMethods = paymentMethods,
            appcBalance = "${balance.walletBalance.creditsBalance.fiat.symbol} ${
              currencyFormatUtils.formatCurrency(
                balance.walletBalance.creditsBalance.fiat.amount
              )
            }",
          )
        }

      } catch (e: Throwable) {
        viewModelState.update { PaymentMethodsUiState.PaymentMethodsError }
      }
    }
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
          getPaymentMethodsUseCase = injectionsProvider.getPaymentMethodsUseCase,
          getBalanceUseCase = injectionsProvider.getBalanceUseCase,
          currencyFormatUtils = injectionsProvider.currencyFormatUtils,
        ) as T
      }
    }
  )
}

@HiltViewModel
private class PaymentMethodsViewModelInjectionsProvider @Inject constructor(
  val getPaymentMethodsUseCase: GetPaymentMethodsUseCase,
  val getBalanceUseCase: GetBalanceUseCase,
  val currencyFormatUtils: CurrencyFormatUtils,
) : ViewModel()
