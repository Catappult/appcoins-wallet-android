package com.asfoundation.wallet.advertise

import com.asfoundation.wallet.poa.PoaInformationModel
import com.asfoundation.wallet.poa.ProofSubmissionData
import io.reactivex.Single

interface Advertising {

  fun getCampaign(packageName: String, versionCode: Int): Single<CampaignDetails>
  fun hasSeenPoaNotificationTimePassed(): Boolean
  fun clearSeenPoaNotification()
  fun saveSeenPoaNotification()

  enum class CampaignAvailabilityType {
    AVAILABLE, UNAVAILABLE, UNKNOWN_ERROR, NO_INTERNET_CONNECTION, API_ERROR,
    PACKAGE_NAME_NOT_FOUND, UPDATE_REQUIRED
  }

  fun hasWalletPrepared(chainId: Int, packageName: String,
                        versionCode: Int): Single<ProofSubmissionData>

  fun retrievePoaInformation(address: String): Single<PoaInformationModel>
}

data class CampaignDetails(val responseCode: Advertising.CampaignAvailabilityType,
                           val campaignId: String? = "", val hoursRemaining: Int = 0,
                           val minutesRemaining: Int = 0) {

  fun hasReachedPoaLimit() = hoursRemaining != 0 || minutesRemaining != 0
}

