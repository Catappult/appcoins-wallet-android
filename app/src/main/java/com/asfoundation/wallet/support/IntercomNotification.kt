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

  fun sendNotification(remoteMessage: RemoteMessage) {
    intercomPushClient.handlePush(application, remoteMessage.data)
    saveBooleanNotificationToSharedPreferences(application)
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