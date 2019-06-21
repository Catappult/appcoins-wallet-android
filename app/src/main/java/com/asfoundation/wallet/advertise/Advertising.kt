package com.asfoundation.wallet.advertise

import io.reactivex.Single

interface Advertising {

  fun getCampaign(packageName: String, versionCode: Int): Single<CampaignDetails>

  enum class CampaignAvailabilityType {
    AVAILABLE, UNAVAILABLE, UNKNOWN_ERROR, NO_INTERNET_CONNECTION, API_ERROR, PACKAGE_NAME_NOT_FOUND
  }
}

data class CampaignDetails(val responseCode: Advertising.CampaignAvailabilityType,
                           val campaignId: String? = "")