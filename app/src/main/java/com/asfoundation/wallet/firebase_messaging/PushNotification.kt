package com.asfoundation.wallet.firebase_messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.asf.wallet.R
import com.asfoundation.wallet.main.PendingIntentNavigator
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PushNotification @Inject constructor(
  @ApplicationContext private val context: Context,
  private val notificationManager: NotificationManager,
  private val pendingIntentNavigator: PendingIntentNavigator,
) {
  companion object {
    private const val CHANNEL_NAME = "Notification Channel"
    private const val CHANNEL_ID = "notification_channel_push"
  }

  fun sendPushNotification(remoteMessage: RemoteMessage) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = createNotificationChannel()
      notificationManager.createNotificationChannel(channel)
    }

    val code = remoteMessage.data["code"]?.toInt() ?: 0

    val notification = buildNotification(
      title = remoteMessage.data["title"].toString(),
      message = remoteMessage.data["message"].toString(),
    )

    notificationManager.notify(
      code,
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

  private fun buildNotification(title: String, message: String) =
    NotificationCompat.Builder(context, CHANNEL_ID)
      .setAutoCancel(true)
      .setContentIntent(pendingIntentNavigator.getHomePendingIntent())
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setSmallIcon(R.drawable.ic_appcoins_notification_icon)
      .setContentTitle(title)
      .setContentText(message)
      .apply { if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) setVibrate(LongArray(0)) }
      .build()
}
