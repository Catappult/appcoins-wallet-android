package com.asfoundation.wallet.promotions.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ShareCompat
import com.asf.wallet.R
import com.asfoundation.wallet.main.PendingIntentNavigator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PromotionNotification @Inject constructor(
  @ApplicationContext private val context: Context,
  private val notificationManager: NotificationManager,
  private val pendingIntentNavigator: PendingIntentNavigator,
) {
  companion object {
    private const val CHANNEL_NAME = "VIP Referral Notification Channel"
    private const val CHANNEL_ID = "notification_channel_vip_referral"
    private const val NOTIFICATION_SERVICE_ID = 77777
  }

  fun sendPromotionNotification(code: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = createNotificationChannel()
      notificationManager.createNotificationChannel(channel)
    }

    val notification = buildNotification(code)

    notificationManager.notify(
      NOTIFICATION_SERVICE_ID,
      notification
    )
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel(): NotificationChannel {
    return NotificationChannel(
      CHANNEL_ID,
      CHANNEL_NAME,
      NotificationManager.IMPORTANCE_HIGH
    )
  }

  private fun buildNotification(code: String) =
    NotificationCompat.Builder(context, CHANNEL_ID)
      .setAutoCancel(true)
      .setContentIntent(pendingIntentNavigator.getHomePendingIntent())
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setSmallIcon(R.drawable.ic_appcoins_notification_icon)
      .setContentTitle(context.getString(R.string.vip_program_referral_notification_title))
      .setContentText(context.getString(R.string.vip_program_referral_notification_body))
      .addAction(
        android.R.drawable.ic_menu_share,
        context.getString(R.string.wallet_view_share_button),
        getSharePendingIntent(code)
      )
      .apply { if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) setVibrate(LongArray(0)) }
      .build()

  private fun getSharePendingIntent(code: String) =
    PendingIntent.getActivity(
      context,
      0,
      ShareCompat.IntentBuilder(context)
        .setText(code)
        .setType("text/plain")
        .setChooserTitle(context.getString(R.string.share_via))
        .intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
