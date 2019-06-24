package com.asfoundation.wallet.advertise

import android.content.Intent
import android.os.IBinder
import dagger.android.DaggerService
import javax.inject.Inject

class AdvertisingService : DaggerService() {
  @Inject
  lateinit var campaignInteract: CampaignInteract

  override fun onBind(intent: Intent): IBinder {
    return AppCoinsAdvertisingBinder(applicationContext.packageManager, campaignInteract)
  }
}
