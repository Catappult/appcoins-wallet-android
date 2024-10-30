package com.asfoundation.wallet.iab.presentation.payment_methods

import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData

sealed class PaymentMethodsUiState {
  data class LoadingPaymentMethods(val purchaseInfo: PurchaseInfoData) : PaymentMethodsUiState()
  data class PaymentMethodsIdle(
    val purchaseInfo: PurchaseInfoData,
    val paymentMethods: List<PaymentMethod>,
  ) : PaymentMethodsUiState()

  data object PaymentMethodsError : PaymentMethodsUiState()
  data object NoConnection : PaymentMethodsUiState()
}
