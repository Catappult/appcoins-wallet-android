package com.asfoundation.wallet.backup

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.asfoundation.wallet.backup.BackupNotificationUtils.NOTIFICATION_SERVICE_ID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupBroadcastReceiver : BroadcastReceiver() {

  @Inject
  lateinit var backupInteract: BackupInteractContract

  private lateinit var notificationManager: NotificationManager

  companion object {

    private const val WALLET_ADDRESS = "wallet_address"
    private const val ACTION = "extra_action"
    const val ACTION_BACKUP = "action_backup"
    const val ACTION_DISMISS = "action_dismiss"

    @JvmStatic
    fun newIntent(context: Context, walletAddress: String, action: String) =
        Intent(context, BackupBroadcastReceiver::class.java).apply {
          putExtra(WALLET_ADDRESS, walletAddress)
          putExtra(ACTION, action)
          flags = FLAG_ACTIVITY_NEW_TASK
        }
  }

  override fun onReceive(context: Context, intent: Intent) {
    notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.cancel(NOTIFICATION_SERVICE_ID)
    context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))

    val wallet = intent.getStringExtra(WALLET_ADDRESS)
    wallet?.let {
      backupInteract.saveDismissSystemNotification(it)

      if (intent.getStringExtra(ACTION) == ACTION_BACKUP) {
        val backupIntent = BackupActivity.newIntent(context, it)
            .apply {
              flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        context.startActivity(backupIntent)
      } else if (intent.getStringExtra(ACTION) == ACTION_DISMISS) return
    }
  }

}
