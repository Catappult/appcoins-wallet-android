package com.asfoundation.wallet.promotions.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Completable
import java.util.Date
import javax.inject.Inject

class NotificationScheduler @Inject constructor(
  @ApplicationContext private val context: Context,
  private val alarmManager: AlarmManager,
  private val stringToIntConverter: StringToIntConverter,
) {

  fun scheduleNotification(
    walletAddress: String,
    date: Date?,
    vipReferralCode: String,
  ) = Completable.create { emitter ->
    val scheduleDate = date.takeIf { it != null } ?: return@create emitter.onComplete()

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      stringToIntConverter.getStringId(walletAddress),
      PromotionBroadcastReceiver.createPendingIntent(context, vipReferralCode),
      // This is essential to be like that for having extras in the intent
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
      } else {
        PendingIntent.FLAG_UPDATE_CURRENT
      }
    )

    alarmManager.setExactAndAllowWhileIdle(
      AlarmManager.RTC_WAKEUP,
      scheduleDate.time,
      pendingIntent
    )

    emitter.onComplete()
  }
}
