package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

//type=TOPUP&status=COMPLETED&wallet_from=0xcA8c45737325B04DE6A2b986EbffEEfB51D0FA10
/** Copied from com.appcoins.wallet.bdsbilling.repository.entity*/
data class TransactionPrice(val currency: String, val value: String, val appc: String)

data class TopUpResponse(
  val items: List<Transaction>? = null
)