package com.asfoundation.wallet.billing.paypal

enum class PaypalReturnSchemas(val schema: String) {
  RETURN("paypalsuccess://"),
  CANCEL("paypalcancel://")
}