package com.asfoundation.wallet.iab.presentation.payment_methods

import com.asfoundation.wallet.iab.presentation.PaymentMethodData
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData

sealed class PaymentMethodsUiState {
  data class LoadingPaymentMethods(val purchaseInfo: PurchaseInfoData) : PaymentMethodsUiState()
  data class PaymentMethodsIdle(
    val purchaseInfo: PurchaseInfoData,
    val paymentMethods: List<PaymentMethodData>,
  ) : PaymentMethodsUiState()

  data object PaymentMethodsError : PaymentMethodsUiState()
  data object NoConnection : PaymentMethodsUiState()
}
