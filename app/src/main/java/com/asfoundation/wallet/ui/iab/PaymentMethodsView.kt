package com.asfoundation.wallet.ui.iab

interface PaymentMethodsView {
  fun showPaymentMethods(paymentMethods: List<PaymentMethod>)
  fun showError()
}
