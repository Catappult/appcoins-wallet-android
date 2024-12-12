package com.asfoundation.wallet.billing.amazonPay

data class SuccessInfo(
  val hash: String?,
  val orderReference: String?,
  val purchaseUid: String?,
)