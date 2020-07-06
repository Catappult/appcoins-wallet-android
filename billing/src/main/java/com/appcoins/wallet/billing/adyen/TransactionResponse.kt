package com.appcoins.wallet.billing.adyen

import com.google.gson.annotations.SerializedName

data class TransactionResponse(val uid: String, val hash: String,
                               @SerializedName("reference") val orderReference: String?,
                               val status: Status) {

  enum class Status {
    PENDING, PENDING_SERVICE_AUTHORIZATION, SETTLED, PROCESSING, COMPLETED, PENDING_USER_PAYMENT,
    INVALID_TRANSACTION, FAILED, CANCELED, FRAUD
  }


}
