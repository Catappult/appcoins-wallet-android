package com.asfoundation.wallet.onboarding_new_payment.payment_methods.list

sealed class PaymentMethodClick {
  object CreditCardClick : PaymentMethodClick()
  object PaypalAdyenClick : PaymentMethodClick()
  object GiroPayAdyenClick : PaymentMethodClick()
  object KakaoPayAdyenClick : PaymentMethodClick()
  object PaypalDirectClick : PaymentMethodClick()
  data class LocalPaymentClick(val idItem: String) : PaymentMethodClick()
  object CarrierBillingClick : PaymentMethodClick()
  object ShareLinkPaymentClick : PaymentMethodClick()
  object OtherPaymentMethods : PaymentMethodClick()
}