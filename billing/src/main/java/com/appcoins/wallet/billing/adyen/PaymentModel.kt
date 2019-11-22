package com.appcoins.wallet.billing.adyen

data class PaymentModel(val resultCode: String, val refusalReason: String?, val refusalCode: Int?,
                        val type: String?, val redirectUrl: String?, val error: Boolean = false) {
  constructor() : this("", null, null, null, null, true)
}
