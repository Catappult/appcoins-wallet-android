package com.asfoundation.wallet.backup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupBroadcastReceiver.Companion.ACTION_BACKUP
import com.asfoundation.wallet.backup.BackupBroadcastReceiver.Companion.ACTION_DISMISS

object BackupNotificationUtils {

  const val NOTIFICATION_SERVICE_ID = 77795
  private const val CHANNEL_ID = "backup_notification_channel_id"
  private const val CHANNEL_NAME = "Backup Notification Channel"

  private lateinit var notificationManager: NotificationManager

  fun showBackupNotification(context: Context, walletAddress: String) {
    notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val backupIntent = createNotificationBackupIntent(context, walletAddress)
    val dismissIntent = createNotificationDismissIntent(context, walletAddress)
    val builder: NotificationCompat.Builder
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
      builder = NotificationCompat.Builder(context, CHANNEL_ID)
      notificationManager.createNotificationChannel(notificationChannel)
    } else {
      builder = NotificationCompat.Builder(context, CHANNEL_ID)
    }

    val notification =
        builder.setContentTitle(context.getString(R.string.backup_notification_title))
          .setAutoCancel(true)
          .setContentIntent(backupIntent)
          .addAction(0, context.getString(R.string.dismiss_button), dismissIntent)
          .addAction(0, context.getString(R.string.action_backup_wallet), backupIntent)
            .setSmallIcon(R.drawable.ic_appcoins_notification_icon)
            .setDeleteIntent(dismissIntent)
            .setContentText(context.getString(R.string.backup_notification_body))
            .build()

    notificationManager.notify(NOTIFICATION_SERVICE_ID, notification)
  }

  private fun createNotificationBackupIntent(context: Context,
                                             walletAddress: String): PendingIntent {
    val intent = BackupBroadcastReceiver.newIntent(context, walletAddress, ACTION_BACKUP)
    return PendingIntent.getBroadcast(
      context,
      0,
      intent,
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      else
        PendingIntent.FLAG_UPDATE_CURRENT)
  }

  private fun createNotificationDismissIntent(context: Context,
                                              walletAddress: String): PendingIntent {
    val intent = BackupBroadcastReceiver.newIntent(context, walletAddress, ACTION_DISMISS)
    return PendingIntent.getBroadcast(
      context,
      1,
      intent,
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      else
        PendingIntent.FLAG_UPDATE_CURRENT)
  }

}