package com.asfoundation.wallet.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.asfoundation.wallet.router.OnboardingRouter
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.UpdateRequiredActivity

class SplashNavigator(private val activity: AppCompatActivity) {

  fun firstScreenNavigation(shouldShowOnboarding: Boolean) {
    val fromSupportNotification =
        activity.intent.getBooleanExtra(SUPPORT_NOTIFICATION_CLICK, false)
    if (shouldShowOnboarding) {
      OnboardingRouter().open(activity, true, fromSupportNotification)
    } else {
      TransactionsRouter().navigateFromSplash(activity, fromSupportNotification)
    }
    activity.finish()
  }

  fun showAuthenticationActivity() {
    val intent = AuthenticationPromptActivity.newIntent(activity)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    activity.startActivityForResult(intent, AUTHENTICATION_REQUEST_CODE)
  }

  fun navigateToAutoUpdate() {
    val intent = UpdateRequiredActivity.newIntent(activity)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
    activity.startActivity(intent)
    activity.finish()
  }

  fun finish() = activity.finish()

  companion object {
    const val AUTHENTICATION_REQUEST_CODE = 33
  }
}
