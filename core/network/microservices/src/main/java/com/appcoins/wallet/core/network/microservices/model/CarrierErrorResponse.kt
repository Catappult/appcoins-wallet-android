package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CarrierErrorResponse(val code: String? = null,
                                val path: String? = null,
                                val text: String? = null,
                                val data: List<Data>? = null) {

  data class Data(val name: String?,
                  val type: String?,
                  val value: BigDecimal?,
                  val messages: Messages?) {
    data class Messages(@SerializedName("enduser") val endUser: String?, val technical: String?)
  }
}