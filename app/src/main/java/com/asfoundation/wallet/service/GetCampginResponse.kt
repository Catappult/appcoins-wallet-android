package com.asfoundation.wallet.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetCampaignResponse(@JsonProperty("status") val status: EligibleResponseStatus, @JsonProperty("bid_id") val bidId: String?) {

  enum class EligibleResponseStatus {
    ELIGIBLE, NOT_ELIGIBLE, REQUIRES_VALIDATION
  }
}


