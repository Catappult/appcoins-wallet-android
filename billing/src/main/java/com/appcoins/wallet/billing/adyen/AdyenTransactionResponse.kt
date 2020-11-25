package com.appcoins.wallet.billing.adyen

import com.appcoins.wallet.billing.common.response.TransactionMetadata
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.google.gson.annotations.SerializedName

data class AdyenTransactionResponse(val uid: String, val hash: String?,
                                    @SerializedName("reference") val orderReference: String?,
                                    val status: TransactionStatus,
                                    val payment: MakePaymentResponse?,
                                    val metadata: TransactionMetadata?)