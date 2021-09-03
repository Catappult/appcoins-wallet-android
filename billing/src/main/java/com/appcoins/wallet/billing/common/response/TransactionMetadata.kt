package com.appcoins.wallet.billing.common.response

import com.google.gson.annotations.SerializedName

data class TransactionMetadata(@SerializedName("error_message") val errorMessage: String?,
                               @SerializedName("error_code") val errorCode: Int?)