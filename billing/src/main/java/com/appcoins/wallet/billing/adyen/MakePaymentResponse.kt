package com.appcoins.wallet.billing.adyen

data class MakePaymentResponse(val resultCode: String, val refusalReason: String?,
                               val refusalReasonCode: String?, val action: Action?)

data class Action(val type: String?, val url: String?)