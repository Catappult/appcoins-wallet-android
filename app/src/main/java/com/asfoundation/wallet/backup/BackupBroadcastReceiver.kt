package com.asfoundation.wallet.backup

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.asfoundation.wallet.backup.BackupNotificationUtils.NOTIFICATION_SERVICE_ID
import com.asfoundation.wallet.ui.backup.BackupActivity
import dagger.android.AndroidInjection
import dagger.android.DaggerBroadcastReceiver
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class BackupBroadcastReceiver : DaggerBroadcastReceiver(), HasAndroidInjector {

  @Inject
  lateinit var androidInjector: DispatchingAndroidInjector<Any>

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
    super.onReceive(context, intent)
    AndroidInjection.inject(this, context)
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

  override fun androidInjector() = androidInjector

}
