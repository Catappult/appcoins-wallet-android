package com.appcoins.wallet.billing.adyen

import com.google.gson.annotations.SerializedName

data class AdyenTransactionResponse(val uid: String, val hash: String?,
                                    @SerializedName("reference") val orderReference: String?,
                                    val status: TransactionResponse.Status,
                                    val payment: MakePaymentResponse,
                                    val metadata: Metadata?)

data class Metadata(@SerializedName("error_message") val errorMessage: String?,
                    @SerializedName("error_code") val errorCode: Int?)