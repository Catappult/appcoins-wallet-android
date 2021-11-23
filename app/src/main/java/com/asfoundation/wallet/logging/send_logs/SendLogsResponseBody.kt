package com.asfoundation.wallet.logging.send_logs

import com.google.gson.annotations.SerializedName

data class CanLogResponse(
    val logging: Boolean)

data class GetSendLogsUrlResponse(
    val url: String,
    val fields: FieldsItem
)

data class FieldsItem(
    var key: String,
    @SerializedName("AWSAccessKeyId") var awsAccessKeyId: String,
    var policy: String,
    var signature: String,
    @SerializedName("x-amz-security-token") var token: String?,
)