package com.asfoundation.wallet.advertise

import android.app.NotificationManager
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.asfoundation.wallet.interact.AutoUpdateInteract
import dagger.android.DaggerService
import javax.inject.Inject
import javax.inject.Named

class AdvertisingService : DaggerService() {
  @Inject
  lateinit var campaignInteract: CampaignInteract
  @Inject
  lateinit var autoUpdateInteract: AutoUpdateInteract
  @Inject
  lateinit var notificationManager: NotificationManager
  @field:[Inject Named("heads_up")]
  lateinit var headsUpNotificationBuilder: NotificationCompat.Builder

  override fun onBind(intent: Intent): IBinder {
    return AppCoinsAdvertisingBinder(applicationContext.packageManager, campaignInteract,
        autoUpdateInteract, notificationManager, headsUpNotificationBuilder, applicationContext)
  }
}
