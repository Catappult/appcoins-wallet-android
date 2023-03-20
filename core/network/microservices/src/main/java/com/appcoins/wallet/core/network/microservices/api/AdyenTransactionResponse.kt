package com.appcoins.wallet.core.network.microservices.api

import com.appcoins.wallet.core.network.microservices.model.MakePaymentResponse
import com.appcoins.wallet.core.network.microservices.model.TransactionMetadata
import com.appcoins.wallet.core.network.microservices.model.TransactionStatus
import com.google.gson.annotations.SerializedName

data class AdyenTransactionResponse(val uid: String, val hash: String?,
                                    @SerializedName("reference") val orderReference: String?,
                                    val status: TransactionStatus,
                                    val payment: MakePaymentResponse?,
                                    val metadata: TransactionMetadata?)