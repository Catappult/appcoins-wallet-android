package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class TransactionResponse(val uid: String, val hash: String,
                               @SerializedName("reference") val orderReference: String?,
                               val status: TransactionStatus, val metadata: TransactionMetadata?)