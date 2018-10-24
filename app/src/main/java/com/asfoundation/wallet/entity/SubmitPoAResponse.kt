package com.asfoundation.wallet.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubmitPoAResponse(@JsonProperty("txid") val transactionId: String, @JsonProperty("valid")
val isValid: Boolean, @JsonProperty("error_code") val errorCode: Int)