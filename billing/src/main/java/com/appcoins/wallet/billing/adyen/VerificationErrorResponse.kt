package com.appcoins.wallet.billing.adyen

data class VerificationErrorResponse(val code: String? = null,
                                     val path: String? = null,
                                     val text: String? = null,
                                     val data: Data? = null) {

  data class Data(val enduser: String?,
                  val technical: String?)
}