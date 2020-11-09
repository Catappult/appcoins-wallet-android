package com.appcoins.wallet.billing.carrierbilling.response

import com.google.gson.annotations.SerializedName

data class CarrierTransactionErrorResponse(val code: String, val path: String, val text: String,
                                           @SerializedName("data")
                                           val error: TransactionCarrierError)