package com.appcoins.wallet.bdsbilling.repository.entity

import com.google.gson.annotations.SerializedName


data class Transaction(val uid: String,
                       val status: Status,
                       val gateway: Gateway?,
                       var hash: String?,
                       val metadata: Metadata?,
                       val orderReference: String?,
                       val price: TransactionPrice?,
                       val type: String,
                       val url: String? = null) {

  companion object {
    fun notFound(): Transaction {
      return Transaction("", Status.INVALID_TRANSACTION, Gateway.unknown(), null, null, null, null,
          "", "")
    }

  }

  enum class Status {
    PENDING, PENDING_SERVICE_AUTHORIZATION, PROCESSING, COMPLETED, PENDING_USER_PAYMENT,
    INVALID_TRANSACTION, FAILED, CANCELED, FRAUD, SETTLED
  }

}

data class Metadata(@SerializedName("purchase_uid") val purchaseUid: String)

data class TransactionPrice(val currency: String, val value: String, val appc: String)