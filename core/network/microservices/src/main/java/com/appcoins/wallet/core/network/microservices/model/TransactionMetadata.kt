package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class TransactionMetadata(@SerializedName("error_message") val errorMessage: String?,
                               @SerializedName("error_code") val errorCode: Int?,
                               @SerializedName("purchase_uid") val purchaseUid: String?)