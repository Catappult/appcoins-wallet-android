package com.asfoundation.wallet.backup

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.appcoins.wallet.feature.backup.data.use_cases.SaveDismissSystemNotificationUseCase
import com.appcoins.wallet.feature.backup.ui.BackupActivity
import com.asfoundation.wallet.backup.BackupNotificationUtils.NOTIFICATION_SERVICE_ID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupBroadcastReceiver : ComponentActivity() {

  @Inject
  lateinit var saveDismissSystemNotificationUseCase: SaveDismissSystemNotificationUseCase

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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.cancel(NOTIFICATION_SERVICE_ID)

    val wallet = intent.getStringExtra(WALLET_ADDRESS)
    wallet?.let {
      when (intent.getStringExtra(ACTION)) {
        ACTION_BACKUP -> {
          val backupIntent = BackupActivity.newIntent(this, it, isBackupTrigger = true)
            .apply {
              flags = FLAG_ACTIVITY_NEW_TASK
            }
          startActivity(backupIntent)
        }
        ACTION_DISMISS -> {
          saveDismissSystemNotificationUseCase(it)
        }
      }
    }
    finishAffinity()
  }
}
