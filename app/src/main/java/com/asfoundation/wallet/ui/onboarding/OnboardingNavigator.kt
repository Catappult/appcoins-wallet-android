package com.asfoundation.wallet.ui.onboarding

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.asf.wallet.R
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.support.SupportNotificationProperties

class OnboardingNavigator(private val activity: AppCompatActivity,
                          private val transactionsRouter: TransactionsRouter) {

  fun launchBrowser(uri: String) {
    try {
      val launchBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
      launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      activity.startActivity(launchBrowser)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      Toast.makeText(activity, R.string.unknown_error, Toast.LENGTH_SHORT)
          .show()
    }
  }

  fun navigateToTransactions() {
    val fromSupportNotification =
        activity.intent.getBooleanExtra(SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK,
            false)
    transactionsRouter.navigateFromSplash(activity, fromSupportNotification)
    activity.finish()
  }
}
