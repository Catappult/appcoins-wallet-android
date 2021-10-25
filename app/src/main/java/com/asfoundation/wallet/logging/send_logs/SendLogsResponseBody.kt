package com.asfoundation.wallet.logging.send_logs

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class CanLogResponse(
    @JsonProperty("logging") @SerializedName("logging") val logging: Boolean)



