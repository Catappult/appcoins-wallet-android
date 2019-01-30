package com.appcoins.wallet.billing.repository.entity

import com.google.gson.annotations.SerializedName

class TransactionData @JvmOverloads constructor(@SerializedName("type")
                                                private val _type: String? = UNKNOWN,
                                                @SerializedName("domain")
                                                private val _domain: String? = UNKNOWN,
                                                @SerializedName("skuId") private val _skuId: String,
                                                @SerializedName("payload")
                                                private val _payload: String? = null,
                                                @SerializedName("order_reference")
                                                private val _orderReference: String? = null,
                                                @SerializedName("origin")
                                                private val _origin: String? = null) {

  val type get() = _type ?: UNKNOWN
  val domain get() = _domain ?: UNKNOWN
  val skuId get() = _skuId
  val payload get() = _payload
  val orderReference get() = _orderReference
  val origin get() = _origin

  companion object {
    const val UNKNOWN = "unknown"
  }

  enum class TransactionType {
    DONATION,
    INAPP,
    INAPP_UNMANAGED;
  }
}
