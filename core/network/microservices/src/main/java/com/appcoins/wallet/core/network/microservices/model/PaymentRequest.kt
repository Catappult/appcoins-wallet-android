package com.appcoins.wallet.core.network.microservices.model

data class PaymentRequest(
  var payment: Payment,
  var product_token: String
)

data class Payment(
  var method: Method,
  var return_url: String
)

data class Method(
  var type: String,
  var encryptedCardNumber: String?,
  var encryptedExpiryMonth: String?,
  var encryptedExpiryYear: String?,
  var encryptedSecurityCode: String
)