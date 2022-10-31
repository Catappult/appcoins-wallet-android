package com.asfoundation.wallet.billing.paypal

import com.appcoins.wallet.billing.common.response.TransactionStatus

data class PaypalV2StartResponse(
  val uid: String,
  val hash: String?,
  val status: TransactionStatus
)