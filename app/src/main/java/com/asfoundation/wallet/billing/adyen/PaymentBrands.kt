package com.asfoundation.wallet.billing.adyen

enum class PaymentBrands(val brandName: String) {
  MASTERCARD("mc"),
  VISA("visa"),
  AMEX("amex"),
  MAESTRO("maestro"),
  DINERS("diners"),
  DISCOVER("discover")
}