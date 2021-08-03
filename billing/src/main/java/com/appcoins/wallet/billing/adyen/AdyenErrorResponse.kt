package com.appcoins.wallet.billing.adyen

data class AdyenErrorResponse(val code: String? = null,
                              val path: String? = null,
                              val text: String? = null,
                              val data: Int? = null) {
}