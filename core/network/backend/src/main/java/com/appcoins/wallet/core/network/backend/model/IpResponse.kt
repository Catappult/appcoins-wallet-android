package com.appcoins.wallet.core.network.backend.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
class IpResponse {
  @JsonProperty("countryCode")
  var countryCode: String? = null
}