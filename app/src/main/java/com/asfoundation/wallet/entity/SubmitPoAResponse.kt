package com.asfoundation.wallet.entity

import com.fasterxml.jackson.annotation.JsonProperty

class SubmitPoAResponse(@JsonProperty("txid") val transactionId: String, @JsonProperty("valid")
val isValid: Boolean, @JsonProperty("error_code") val errorCode: Int)