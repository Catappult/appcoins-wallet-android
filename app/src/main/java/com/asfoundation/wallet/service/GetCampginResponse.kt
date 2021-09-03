package com.asfoundation.wallet.service

import com.google.gson.annotations.SerializedName

data class GetCampaignResponse(@SerializedName("status") val status: EligibleResponseStatus,
                               @SerializedName("bid_id") val bidId: String?, val hours: Int,
                               val minutes: Int) {

  enum class EligibleResponseStatus {
    ELIGIBLE, NOT_ELIGIBLE, REQUIRES_VALIDATION
  }
}


