package com.asfoundation.wallet.support

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.support.SupportNotificationProperties.ACTION_CHECK_MESSAGES
import com.asfoundation.wallet.support.SupportNotificationProperties.ACTION_DISMISS
import com.asfoundation.wallet.support.SupportNotificationProperties.ACTION_KEY
import com.asfoundation.wallet.support.SupportNotificationProperties.NOTIFICATION_SERVICE_ID

class SupportNotificationBroadcastReceiver : BroadcastReceiver() {

  private lateinit var notificationManager: NotificationManager

  companion object {

    @JvmStatic
    fun newIntent(context: Context) =
        Intent(context, SupportNotificationBroadcastReceiver::class.java)
  }

  override fun onReceive(context: Context, intent: Intent) {
    notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.cancel(NOTIFICATION_SERVICE_ID)
    context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))

    when (intent.getStringExtra(ACTION_KEY)) {
      ACTION_CHECK_MESSAGES -> onNotificationClicked(context)
      ACTION_DISMISS -> return
    }
  }

  private fun onNotificationClicked(context: Context) {
    navigateToIntercomScreen(context)
  }

  private fun navigateToIntercomScreen(context: Context) {
    val transactionsIntent = MainActivity.newIntent(context, true)
        .apply {
          addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
          addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    context.startActivity(transactionsIntent)
  }
}