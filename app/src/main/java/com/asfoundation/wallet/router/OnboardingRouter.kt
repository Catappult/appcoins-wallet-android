package com.asfoundation.wallet.router

import android.content.Context
import android.content.Intent
import com.asfoundation.wallet.ui.onboarding.OnboardingActivity

class OnboardingRouter {

  fun open(context: Context, isClearStack: Boolean,
           fromSupportNotification: Boolean = false) {
    val intent = OnboardingActivity.newIntent(context, fromSupportNotification)
    if (isClearStack) {
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    context.startActivity(intent)
  }
}