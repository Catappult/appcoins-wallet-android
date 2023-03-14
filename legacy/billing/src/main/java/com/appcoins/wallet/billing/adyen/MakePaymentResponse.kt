package com.appcoins.wallet.billing.adyen

import com.google.gson.JsonObject

data class MakePaymentResponse(val pspReference: String, val resultCode: String,
                               val action: JsonObject?, val refusalReason: String?,
                               val refusalReasonCode: String?,
                               val fraudResult: FraudResultResponse?)