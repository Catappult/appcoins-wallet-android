package com.appcoins.wallet.billing.adyen

data class MakePaymentResponse(val resultCode: String, val pspReference: String, val action: Action)

data class Action(val type: String, val url: String)