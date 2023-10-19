package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class VkTransactionResponse(val uid: String, val hash: String?,
                                 @SerializedName("reference") val orderReference: String?,
                                 val status: TransactionStatus,
                                 val amount: Int?)
