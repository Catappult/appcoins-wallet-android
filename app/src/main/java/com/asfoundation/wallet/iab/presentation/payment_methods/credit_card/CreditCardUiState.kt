package com.asfoundation.wallet.iab.presentation.payment_methods.credit_card

import androidx.activity.ComponentActivity
import com.adyen.checkout.card.CardComponent
import com.asfoundation.wallet.iab.payment_manager.payment_methods.CreditCardPaymentMethod
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData

sealed class CreditCardUiState {
  data object Loading : CreditCardUiState()
  data class Idle(
    val creditCardPaymentMethod: CreditCardPaymentMethod,
    val purchaseInfoData: PurchaseInfoData,
    val cardComponent: ((ComponentActivity) -> CardComponent?)?,
    val savingCreditCard: Boolean
  ) : CreditCardUiState()

  data object PaymentMethodsError : CreditCardUiState()
  data object NoConnection : CreditCardUiState()
  data object Finish : CreditCardUiState()
}