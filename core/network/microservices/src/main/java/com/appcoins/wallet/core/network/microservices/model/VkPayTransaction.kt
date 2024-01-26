package com.appcoins.wallet.core.network.microservices.model

data class VkPayTransaction(
  val uid: String?,
  val hash: String?,
  val status: TransactionStatus?,
  val amount: Int?,
  val errorCode: String? = null,
  val errorMessage: String? = null
)
