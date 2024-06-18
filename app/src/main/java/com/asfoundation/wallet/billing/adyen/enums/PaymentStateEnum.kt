package com.asfoundation.wallet.billing.adyen.enums

enum class PaymentStateEnum(val state: String) {
  PAYMENT_WITH_NEW_CARD("payment_with_new_card"),
  PAYMENT_WITH_STORED_CARD_ID("payment_with_stored_card_id"),
  PAYMENT_WITH_PAYPAL("payment_with_paypal"),
  UNDEFINED("undefined")
}