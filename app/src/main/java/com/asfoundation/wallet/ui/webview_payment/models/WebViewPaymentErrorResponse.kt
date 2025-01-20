package com.asfoundation.wallet.ui.webview_payment.models

data class WebViewPaymentErrorResponse(
  val errorCode: String?,
  val errorDetails: String?,
  val paymentMethod: String?,
  val isStoredCard: Boolean?,
  val wasCvcRequired: Boolean?,
)
