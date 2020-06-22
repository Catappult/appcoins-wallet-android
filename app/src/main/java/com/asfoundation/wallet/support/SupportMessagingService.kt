package com.asfoundation.wallet.support

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.intercom.android.sdk.push.IntercomPushClient


class SupportMessagingService : FirebaseMessagingService() {

  private lateinit var notificationManager: NotificationManager
  private val intercomPushClient = IntercomPushClient()

  override fun onNewToken(token: String) {
    Log.d(TAG, "Refreshed token: $token")
    intercomPushClient.sendTokenToIntercom(application, token)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    Log.d(TAG, "From: ${remoteMessage.from}")

    if (remoteMessage.data.isNotEmpty()) {
      Log.d(TAG, "Message data payload: ${remoteMessage.data}")
    }

    remoteMessage.notification?.let {
      Log.d(TAG, "Message Notification Body: ${it.body}")
    }

    if (intercomPushClient.isIntercomPush(remoteMessage.data)) {
      intercomPushClient.handlePush(application, remoteMessage.data)
    } else {
      super.onMessageReceived(remoteMessage)
    }
  }


  companion object {

    private const val TAG = "SupportMessagingService"
  }

}