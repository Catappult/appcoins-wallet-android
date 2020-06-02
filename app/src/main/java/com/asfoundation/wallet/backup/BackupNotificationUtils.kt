package com.asfoundation.wallet.backup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.WalletBackupActivity
import io.reactivex.Completable

object BackupNotificationUtils {

  private const val NOTIFICATION_SERVICE_ID = 77795
  private const val CHANNEL_ID = "backup_notification_channel_id"
  private const val CHANNEL_NAME = "Backup Notification Channel"

  private lateinit var notificationManager: NotificationManager

  fun showBackupNotification(context: Context, walletAddress: String): Completable {
    return Completable.fromAction {
      notificationManager =
          context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val backupIntent = createNotificationBackupIntent(context, walletAddress)
      val dismissIntent = createNotificationDismissIntent(context)
      val builder: NotificationCompat.Builder
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
        builder = NotificationCompat.Builder(context, CHANNEL_ID)
        notificationManager.createNotificationChannel(notificationChannel)
      } else {
        builder = NotificationCompat.Builder(context, CHANNEL_ID)
      }

      //TODO missing content title strings
      //TODO missing content text strings
      val notification =
          builder.setContentTitle(context.getString(R.string.backup_home_notification_title))
              .setAutoCancel(true)
              .setContentIntent(backupIntent)
              .addAction(0, context.getString(R.string.dismiss_button), dismissIntent)
              .setSmallIcon(R.drawable.ic_launcher_foreground)
              .setContentText(context.getString(R.string.backup_home_notification_body))
              .build()

      notificationManager.notify(NOTIFICATION_SERVICE_ID, notification)

    }

  }

  private fun createNotificationBackupIntent(context: Context,
                                             walletAddress: String): PendingIntent {
    val intent = WalletBackupActivity.newIntent(context, walletAddress)
    return PendingIntent.getBroadcast(context, 0, intent, 0)
  }

  private fun createNotificationDismissIntent(context: Context) =
      PendingIntent.getBroadcast(context, 0, Intent(), 0)

}