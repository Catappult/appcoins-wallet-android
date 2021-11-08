package com.asfoundation.wallet.logging.send_logs

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class CanLogResponse(
    @JsonProperty("logging") @SerializedName("logging") val logging: Boolean)

data class GetSendLogsUrlResponse(
    @JsonProperty("url") val url: String,
    @JsonProperty("fields") val fields: FieldsItem
)

data class FieldsItem(
    @JsonProperty("key") var key: String,
    @JsonProperty("AWSAccessKeyId") @SerializedName("AWSAccessKeyId") var awsAccessKeyId: String,
    @JsonProperty("policy") var policy: String,
    @JsonProperty("signature") var signature: String,
)



