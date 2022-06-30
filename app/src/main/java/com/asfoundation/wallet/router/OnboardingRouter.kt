package com.asfoundation.wallet.router

import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.onboarding.OnboardingActivity

class OnboardingRouter {

  fun open(context: Context, isClearStack: Boolean, isOnboardingFromIap : Boolean,
           fromSupportNotification: Boolean = false) {
    val intent = OnboardingActivity.newIntent(context, isOnboardingFromIap, fromSupportNotification)
    if (isClearStack) {
      intent.flags =
          Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    context.startActivity(intent)
  }
}