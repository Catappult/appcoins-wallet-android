package com.asfoundation.wallet.support

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.asf.wallet.R
import com.asfoundation.wallet.support.SupportNotificationProperties.ACTION_CHECK_MESSAGES
import com.asfoundation.wallet.support.SupportNotificationProperties.ACTION_DISMISS
import com.asfoundation.wallet.support.SupportNotificationProperties.ACTION_KEY
import com.asfoundation.wallet.support.SupportNotificationProperties.CHANNEL_ID
import com.asfoundation.wallet.support.SupportNotificationProperties.CHANNEL_NAME
import com.asfoundation.wallet.support.SupportNotificationProperties.NOTIFICATION_SERVICE_ID
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.intercom.android.sdk.push.IntercomPushClient


class SupportMessagingService : FirebaseMessagingService() {

  private lateinit var notificationManager: NotificationManager
  private lateinit var intercomPushClient: IntercomPushClient

  override fun onCreate() {
    super.onCreate()
    intercomPushClient = IntercomPushClient()
  }

  override fun onNewToken(token: String) =
    intercomPushClient.sendTokenToIntercom(application, token)

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (intercomPushClient.isIntercomPush(remoteMessage.data)) {
      if (isSupportMessage(remoteMessage.data)) {
        notificationManager.notify(NOTIFICATION_SERVICE_ID, createNotification(this).build())
      }
    }
  }

  private fun createNotification(context: Context): NotificationCompat.Builder {
    val okPendingIntent = createNotificationClickIntent(context)
    val dismissPendingIntent = createNotificationDismissIntent(context)
    val builder: NotificationCompat.Builder
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
      builder = NotificationCompat.Builder(context, CHANNEL_ID)
      notificationManager.createNotificationChannel(notificationChannel)
    } else {
      builder = NotificationCompat.Builder(context, CHANNEL_ID)
    }
    return builder.setContentTitle(context.getString(R.string.support_new_message_title))
      .setAutoCancel(true)
      .setContentIntent(okPendingIntent)
      .addAction(0, context.getString(R.string.dismiss_button), dismissPendingIntent)
      .setSmallIcon(R.drawable.ic_appcoins_notification_icon)
      .setContentText(context.getString(R.string.support_new_message_button))
  }

  private fun createNotificationClickIntent(context: Context): PendingIntent {
    val intent = com.asfoundation.wallet.support.SupportNotificationBroadcastReceiver.newIntent(context)
    intent.putExtra(ACTION_KEY, ACTION_CHECK_MESSAGES)
    return PendingIntent.getActivity(
      context,
      0,
      intent,
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        PendingIntent.FLAG_IMMUTABLE
      else
        0
    )
  }

  private fun createNotificationDismissIntent(context: Context): PendingIntent {
    val intent = com.asfoundation.wallet.support.SupportNotificationBroadcastReceiver.newIntent(context)
    intent.putExtra(ACTION_KEY, ACTION_DISMISS)
    return PendingIntent.getActivity(
      context,
      1,
      intent,
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        PendingIntent.FLAG_IMMUTABLE
      else
        0
    )
  }

  private fun isSupportMessage(data: MutableMap<String, String>): Boolean {
    val type = data["conversation_part_type"]
    return type != null && (type == "message" || type == "comment")
  }

}