package com.asfoundation.wallet.advertise

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.interact.CreateWalletInteract
import com.asfoundation.wallet.repository.PreferenceRepositoryType
import com.asfoundation.wallet.service.Campaign
import com.asfoundation.wallet.service.CampaignService
import com.asfoundation.wallet.service.CampaignStatus
import io.reactivex.Single

class CampaignInteract(private val campaignService: CampaignService,
                       private val walletService: WalletService,
                       private val createWalletInteract: CreateWalletInteract,
                       private val errorMapper: AdvertisingThrowableCodeMapper,
                       private val sharedPreferenceRepository: PreferenceRepositoryType) :
    Advertising {

  override fun getCampaign(packageName: String, versionCode: Int): Single<CampaignDetails> {
    return walletService.getWalletAddress()
        .onErrorResumeNext {
          createWalletInteract.create()
              .map { it.address }
        }
        .flatMap { campaignService.getCampaign(it, packageName, versionCode) }
        .map { map(it) }
        .onErrorReturn { CampaignDetails(errorMapper.map(it)) }
  }

  /**
   * Checks if the user has seen the Poa notification in the last 12h
   **/
  override fun hasSeenPoaNotificationTimePassed(): Boolean {
    val savedTime = sharedPreferenceRepository.getPoaNotificationSeenTime()
    val currentTime = System.currentTimeMillis()
    val timeToShowNextNotificationInMillis = 3600000 * 12
    return currentTime >= savedTime + timeToShowNextNotificationInMillis
  }

  override fun clearSeenPoaNotification() {
    sharedPreferenceRepository.clearPoaNotificationSeenTime()
  }

  override fun saveSeenPoaNotification() {
    sharedPreferenceRepository.setPoaNotificationSeenTime(System.currentTimeMillis())
  }

  private fun map(campaign: Campaign) =
      if (campaign.campaignStatus == CampaignStatus.AVAILABLE) CampaignDetails(
          Advertising.CampaignAvailabilityType.AVAILABLE,
          campaign.campaignId) else CampaignDetails(
          Advertising.CampaignAvailabilityType.UNAVAILABLE,
          campaign.campaignId, campaign.hoursRemaining, campaign.minutesRemaining)
}
