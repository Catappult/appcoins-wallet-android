package com.asfoundation.wallet.entity

import com.google.gson.annotations.SerializedName

data class SubmitPoAResponse(@SerializedName("txid") val transactionId: String,
                             @SerializedName("valid") val isValid: Boolean,
                             @SerializedName("error_code") val errorCode: Int)