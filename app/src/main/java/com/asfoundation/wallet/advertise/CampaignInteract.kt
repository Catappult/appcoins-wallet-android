package com.asfoundation.wallet.advertise

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.interact.CreateWalletInteract
import com.asfoundation.wallet.service.Campaign
import com.asfoundation.wallet.service.CampaignService
import com.asfoundation.wallet.service.CampaignStatus
import io.reactivex.Single

class CampaignInteract(private val campaignService: CampaignService,
                       private val walletService: WalletService,
                       private val createWalletInteract: CreateWalletInteract,
                       private val errorMapper: AdvertisingThrowableCodeMapper) : Advertising {

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

  private fun map(campaign: Campaign) =
      if (campaign.campaignStatus == CampaignStatus.AVAILABLE) CampaignDetails(
          Advertising.CampaignAvailabilityType.AVAILABLE,
          campaign.campaignId) else CampaignDetails(
          Advertising.CampaignAvailabilityType.UNAVAILABLE,
          campaign.campaignId)
}
