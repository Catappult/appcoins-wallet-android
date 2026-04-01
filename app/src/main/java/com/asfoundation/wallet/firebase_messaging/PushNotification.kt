package com.asfoundation.wallet.firebase_messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.appcoins.wallet.core.analytics.analytics.notification.NotificationAnalytics
import com.asf.wallet.R
import com.asfoundation.wallet.main.PendingIntentNavigator
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PushNotification @Inject constructor(
  @param:ApplicationContext private val context: Context,
  private val notificationManager: NotificationManager,
  private val pendingIntentNavigator: PendingIntentNavigator,
  private val notificationAnalytics: NotificationAnalytics,
) {
  fun sendPushNotification(remoteMessage: RemoteMessage) {
    notificationAnalytics.sendNotificationReceivedAnalytics(
      remoteMessage.data[PushNotificationProperties.NOTIFICATION_TYPE_KEY]
        ?: PushNotificationProperties.DEFAULT_NOTIFICATION_TYPE
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = createNotificationChannel()
      notificationManager.createNotificationChannel(channel)
    }

    val notificationType = remoteMessage.data[PushNotificationProperties.NOTIFICATION_TYPE_KEY]
      ?: PushNotificationProperties.DEFAULT_NOTIFICATION_TYPE
    context.getSharedPreferences(PushNotificationProperties.PREFS_NAME, Context.MODE_PRIVATE)
      .edit { putString(PushNotificationProperties.NOTIFICATION_TYPE_KEY, notificationType) }

    val code = remoteMessage.data[PushNotificationProperties.CODE_KEY]?.toInt() ?: 0

    val notification = buildNotification(
      title = remoteMessage.data[PushNotificationProperties.TITLE_KEY].toString(),
      message = remoteMessage.data[PushNotificationProperties.MESSAGE_KEY].toString(),
    )

    notificationManager.notify(code, notification)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel(): NotificationChannel {
    return NotificationChannel(
      PushNotificationProperties.CHANNEL_ID,
      PushNotificationProperties.CHANNEL_NAME,
      NotificationManager.IMPORTANCE_HIGH
    )
  }

  private fun buildNotification(title: String, message: String) =
    NotificationCompat.Builder(context, PushNotificationProperties.CHANNEL_ID)
      .setAutoCancel(true)
      .setContentIntent(pendingIntentNavigator.getHomePendingIntent())
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setSmallIcon(R.drawable.ic_appcoins_notification_icon)
      .setContentTitle(title)
      .setContentText(message)
      .apply { if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) setVibrate(LongArray(0)) }
      .build()
}