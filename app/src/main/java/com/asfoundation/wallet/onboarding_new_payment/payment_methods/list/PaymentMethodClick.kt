package com.asfoundation.wallet.onboarding_new_payment.payment_methods.list

sealed class PaymentMethodClick {
  object CreditCardClick : PaymentMethodClick()
  object PaypalAdyenClick : PaymentMethodClick()
  object ChallengeRewardClick : PaymentMethodClick()
  object PaypalDirectClick : PaymentMethodClick()
  data class LocalPaymentClick(val idItem: String) : PaymentMethodClick()
  object CarrierBillingClick : PaymentMethodClick()
  object ShareLinkPaymentClick : PaymentMethodClick()
  object VkPayPaymentClick : PaymentMethodClick()
  object GooglePayClick : PaymentMethodClick()
  data class MiPayPayClick(val idItem: String) : PaymentMethodClick()
  object OtherPaymentMethods : PaymentMethodClick()
}