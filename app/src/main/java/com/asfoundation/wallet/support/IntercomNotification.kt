package com.asfoundation.wallet.support

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.asf.wallet.R
import com.asfoundation.wallet.support.SupportNotificationProperties.ACTION_CHECK_MESSAGES
import com.asfoundation.wallet.support.SupportNotificationProperties.ACTION_DISMISS
import com.asfoundation.wallet.support.SupportNotificationProperties.ACTION_KEY
import com.asfoundation.wallet.support.SupportNotificationProperties.CHANNEL_ID
import com.asfoundation.wallet.support.SupportNotificationProperties.CHANNEL_NAME
import com.asfoundation.wallet.support.SupportNotificationProperties.NOTIFICATION_SERVICE_ID
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import io.intercom.android.sdk.push.IntercomPushClient
import javax.inject.Inject

class IntercomNotification @Inject constructor(
  @ApplicationContext private val context: Context,
  private val notificationManager: NotificationManager,
  private val intercomPushClient: IntercomPushClient,
) {

  companion object {
    private const val HAS_NOTIFICATION_BADGE = "has_seen_notification_badge"
  }

  private val application
    get() = context as Application

  fun sendNotification() {
    notificationManager.notify(NOTIFICATION_SERVICE_ID, createNotification(application).build())
    saveBooleanNotificationToSharedPreferences(application)
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
    val intent = SupportNotificationBroadcastReceiver.newIntent(context)
    intent.putExtra(ACTION_KEY, ACTION_CHECK_MESSAGES)
    return PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_IMMUTABLE
    )
  }

  private fun createNotificationDismissIntent(context: Context): PendingIntent {
    val intent =
      SupportNotificationBroadcastReceiver.newIntent(context)
    intent.putExtra(ACTION_KEY, ACTION_DISMISS)
    return PendingIntent.getActivity(
      context,
      1,
      intent,
      PendingIntent.FLAG_IMMUTABLE
    )
  }

  fun sendTokenToIntercom(token: String) {
    intercomPushClient.sendTokenToIntercom(application, token)
  }

  fun isIntercomPush(remoteMessage: RemoteMessage) =
    intercomPushClient.isIntercomPush(remoteMessage.data) && isSupportMessage(remoteMessage.data)

  private fun isSupportMessage(data: MutableMap<String, String>): Boolean {
    val type = data["conversation_part_type"]
    return type != null &&
        (type == "message" || type == "comment" || type == "assignment" || type == "conversation")
  }

  private fun saveBooleanNotificationToSharedPreferences(context: Context) {
    val sharedPreferences: SharedPreferences =
      PreferenceManager.getDefaultSharedPreferences(context)
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    editor.putBoolean(HAS_NOTIFICATION_BADGE, true)
    editor.apply()
  }
}