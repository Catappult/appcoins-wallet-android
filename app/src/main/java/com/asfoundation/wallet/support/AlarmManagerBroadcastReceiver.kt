package com.asfoundation.wallet.support

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.asf.wallet.R
import com.wallet.appcoins.feature.support.data.SupportNotificationProperties.ACTION_CHECK_MESSAGES
import com.wallet.appcoins.feature.support.data.SupportNotificationProperties.ACTION_DISMISS
import com.wallet.appcoins.feature.support.data.SupportNotificationProperties.ACTION_KEY
import com.wallet.appcoins.feature.support.data.SupportNotificationProperties.CHANNEL_ID
import com.wallet.appcoins.feature.support.data.SupportNotificationProperties.CHANNEL_NAME
import com.wallet.appcoins.feature.support.data.SupportNotificationProperties.NOTIFICATION_SERVICE_ID
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AlarmManagerBroadcastReceiver : BroadcastReceiver() {

  @Inject
  lateinit var supportInteractor: com.wallet.appcoins.feature.support.data.SupportInteractor

  lateinit var notificationManager: NotificationManager

  companion object {

    @JvmStatic
    fun scheduleAlarm(context: Context) {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

      val intent = Intent(context, AlarmManagerBroadcastReceiver::class.java)

      val pendingIntent =
        PendingIntent.getBroadcast(
          context,
          0,
          intent,
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
          else
            PendingIntent.FLAG_CANCEL_CURRENT
        )

      val repeatInterval = TimeUnit.MINUTES.toMillis(15)
      val triggerTime: Long = SystemClock.elapsedRealtime() + repeatInterval
      alarmManager.setInexactRepeating(
        AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime,
        repeatInterval, pendingIntent
      )
    }

  }

  override fun onReceive(context: Context, intent: Intent) {
    notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (supportInteractor.hasNewUnreadConversations()) {
      supportInteractor.updateUnreadConversations()
      notificationManager.notify(NOTIFICATION_SERVICE_ID, createNotification(context).build())
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
    val intent = com.wallet.appcoins.feature.support.data.SupportNotificationBroadcastReceiver.newIntent(context)
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
    val intent = com.wallet.appcoins.feature.support.data.SupportNotificationBroadcastReceiver.newIntent(context)
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
}