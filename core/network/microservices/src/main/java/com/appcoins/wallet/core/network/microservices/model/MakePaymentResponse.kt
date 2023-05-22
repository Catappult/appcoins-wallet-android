package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.JsonObject

data class MakePaymentResponse(val pspReference: String, val resultCode: String,
                               val action: JsonObject?, val refusalReason: String?,
                               val refusalReasonCode: String?,
                               val fraudResult: FraudResultResponse?)