package com.appcoins.wallet.billing.adyen

import com.appcoins.wallet.billing.common.response.TransactionMetadata
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.google.gson.annotations.SerializedName

data class TransactionResponse(val uid: String, val hash: String,
                               @SerializedName("reference") val orderReference: String?,
                               val status: TransactionStatus, val metadata: TransactionMetadata?)