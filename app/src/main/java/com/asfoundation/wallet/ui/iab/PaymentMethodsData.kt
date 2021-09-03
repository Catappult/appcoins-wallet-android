package com.asfoundation.wallet.ui.iab

data class PaymentMethodsData(
  val appPackage: String,
  val isBds: Boolean,
  val developerPayload: String?,
  val uri: String?,
  val sku: String
)