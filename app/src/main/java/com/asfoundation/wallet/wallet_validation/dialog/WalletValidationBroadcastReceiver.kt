package com.asfoundation.wallet.wallet_validation.dialog

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.advertise.WalletPoAService.VERIFICATION_SERVICE_ID

class WalletValidationBroadcastReceiver : BroadcastReceiver() {

  private lateinit var notificationManager: NotificationManager

  companion object {

    const val ACTION_KEY = "ACTION_KEY"
    const val ACTION_START_VALIDATION = "ACTION_START_VALIDATION"
    const val ACTION_DISMISS = "ACTION_DISMISS"

    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, WalletValidationBroadcastReceiver::class.java)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.cancel(VERIFICATION_SERVICE_ID)
    context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))

    if (intent.getStringExtra(ACTION_KEY) == ACTION_START_VALIDATION) {
      val validationIntent = WalletValidationDialogDialogActivity.newIntent(context)
          .apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
          }
      context.startActivity(validationIntent)
    } else if (intent.getStringExtra(ACTION_KEY) == ACTION_DISMISS) return
  }
}
