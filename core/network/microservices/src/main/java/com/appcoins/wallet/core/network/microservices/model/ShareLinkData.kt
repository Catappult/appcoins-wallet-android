package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class ShareLinkData(
  @SerializedName("package") var packageName: String,
  var sku: String?, @SerializedName("wallet_address")
  var walletAddress: String,
  var message: String?, @SerializedName("price.value")
  var amount: String?, @SerializedName("price.currency")
  var currency: String?, var method: String
)