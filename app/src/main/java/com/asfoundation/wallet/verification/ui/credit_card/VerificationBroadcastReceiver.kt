package com.asfoundation.wallet.verification.ui.credit_card

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class VerificationBroadcastReceiver : BroadcastReceiver() {

  private lateinit var notificationManager: NotificationManager

  companion object {

    const val ACTION_KEY = "ACTION_KEY"
    const val ACTION_START_VERIFICATION = "ACTION_START_VERIFICATION"
    const val ACTION_DISMISS = "ACTION_DISMISS"

    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, VerificationBroadcastReceiver::class.java)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))

    if (intent.getStringExtra(ACTION_KEY) == ACTION_START_VERIFICATION) {
      val verificationIntent = VerificationCreditCardActivity.newIntent(context)
        .apply {
          flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
      context.startActivity(verificationIntent)
    } else if (intent.getStringExtra(ACTION_KEY) == ACTION_DISMISS) return
  }
}
