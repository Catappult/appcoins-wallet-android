package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class AdyenTransactionResponse(
    val uid: String,
    val hash: String?,
    @SerializedName("reference") val orderReference: String?,
    val status: TransactionStatus,
    val payment: MakePaymentResponse?,
    val metadata: TransactionMetadata?
)
