package com.asfoundation.wallet.support

import android.app.NotificationManager
import android.app.SearchManager.ACTION_KEY
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.asfoundation.wallet.backup.BackupNotificationUtils.NOTIFICATION_SERVICE_ID
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.support.SupportNotificationProperties.ACTION_CHECK_MESSAGES
import com.asfoundation.wallet.support.SupportNotificationProperties.ACTION_DISMISS

class SupportNotificationBroadcastReceiver : ComponentActivity() {

  private lateinit var notificationManager: NotificationManager

  companion object {

    @JvmStatic
    fun newIntent(context: Context) =
      Intent(context, SupportNotificationBroadcastReceiver::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    notificationManager.cancel(NOTIFICATION_SERVICE_ID)

    when (intent.getStringExtra(ACTION_KEY)) {
      ACTION_CHECK_MESSAGES -> onNotificationClicked(this)
      ACTION_DISMISS -> Unit
    }
    finishAffinity()
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