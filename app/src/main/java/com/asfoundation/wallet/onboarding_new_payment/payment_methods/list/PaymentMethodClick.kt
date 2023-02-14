package com.asfoundation.wallet.onboarding_new_payment.payment_methods.list

sealed class PaymentMethodClick {
  object CreditCardClick : PaymentMethodClick()
  object PaypalAdyenClick : PaymentMethodClick()
  object PaypalDirectClick : PaymentMethodClick()
  object LocalPaymentClick : PaymentMethodClick()
  object CarrierBillingClick : PaymentMethodClick()
  object ShareLinkPaymentClick : PaymentMethodClick()
  object OtherPaymentMethods : PaymentMethodClick()
}