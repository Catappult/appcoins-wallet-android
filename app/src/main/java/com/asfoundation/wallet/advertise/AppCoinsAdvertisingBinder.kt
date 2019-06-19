package com.asfoundation.wallet.advertise

import android.content.pm.PackageManager
import android.os.Binder
import android.os.Bundle
import com.appcoins.advertising.AppCoinsAdvertising

internal class AppCoinsAdvertisingBinder(private val packageManager: PackageManager,
                                         private val campaignInteract: CampaignInteract) :
    AppCoinsAdvertising.Stub() {

  companion object {
    internal const val RESULT_OK = 0 // success
    internal const val RESULT_SERVICE_UNAVAILABLE = 1 // The network connection is down
    internal const val RESULT_CAMPAIGN_UNAVAILABLE = 2 // The campaign is not available

    internal const val RESPONSE_CODE = "RESPONSE_CODE"
    internal const val CAMPAIGN_ID = "CAMPAIGN_ID"
  }

  override fun getAvailableCampaign(): Bundle {
    val uid = Binder.getCallingUid()
    val pkg = packageManager.getNameForUid(uid)
    val pkgInfo = packageManager.getPackageInfo(pkg, 0)
    return campaignInteract.getCampaign(pkg, pkgInfo.versionCode).map { mapCampaignDetails(it) }
        .blockingGet()
  }

  private fun mapCampaignDetails(details: CampaignDetails): Bundle {
    val bundle = Bundle()
    bundle.putInt(RESPONSE_CODE, mapCampaignAvailability(details.responseCode))
    bundle.putString(CAMPAIGN_ID, details.campaignId)
    return bundle
  }

  private fun mapCampaignAvailability(availabilityType: Advertising.CampaignAvailabilityType)
      : Int =
      when (availabilityType) {
        Advertising.CampaignAvailabilityType.AVAILABLE -> RESULT_OK
        Advertising.CampaignAvailabilityType.UNAVAILABLE -> RESULT_CAMPAIGN_UNAVAILABLE
        else -> RESULT_SERVICE_UNAVAILABLE

      }

}