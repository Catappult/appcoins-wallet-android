package com.appcoins.wallet.billing.adyen

import com.google.gson.annotations.SerializedName

data class AdyenTransactionResponse(val uid: String, val hash: String?,
                                    @SerializedName("reference") val orderReference: String?,
                                    val status: TransactionResponse.Status,
                                    val payment: MakePaymentResponse)