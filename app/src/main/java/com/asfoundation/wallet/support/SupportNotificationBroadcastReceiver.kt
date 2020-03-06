package com.asfoundation.wallet.support

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.ui.TransactionsActivity

class SupportNotificationBroadcastReceiver : BroadcastReceiver() {

  private lateinit var notificationManager: NotificationManager

  companion object {
    const val ACTION_KEY = "ACTION_KEY"
    const val ACTION_CHECK_MESSAGES = "ACTION_CHECK_MESSAGES"
    const val ACTION_DISMISS = "ACTION_DISMISS"
    const val SUPPORT_NOTIFICATION_CLICK = "SUPPORT_NOTIFICATION_CLICK"

    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, SupportNotificationBroadcastReceiver::class.java)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.cancel(SupportNotificationWorker.NOTIFICATION_SERVICE_ID)
    context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))

    when (intent.getStringExtra(
        ACTION_KEY)) {
      ACTION_CHECK_MESSAGES -> onNotificationClicked(context)
      ACTION_DISMISS -> onNotificationDismissed()
    }
  }

  private fun onNotificationDismissed() {
    return
  }

  private fun onNotificationClicked(context: Context) {
    navigateToIntercomScreen(context)
  }

  private fun navigateToIntercomScreen(context: Context) {
    val transactionsIntent =
        TransactionsActivity.newIntent(context, true)
    transactionsIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(transactionsIntent)
  }
}