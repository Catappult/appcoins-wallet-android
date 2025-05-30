package com.asfoundation.wallet.ui.webview_payment.models

data class WebViewPaymentResponse(
  val uid: String?,
  val orderReference: String?,
  val hash: String?,
  val paymentMethod: String?,
  val isStoredCard: Boolean?,
  val wasCvcRequired: Boolean?,
)
