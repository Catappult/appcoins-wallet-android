package com.appcoins.wallet.billing.adyen

data class PaymentModel(val resultCode: String, val pspReference: String, val type: String?,
                        val redirectUrl: String?, val error: Boolean = false) {
  constructor() : this("", "", "", "", true)
}
