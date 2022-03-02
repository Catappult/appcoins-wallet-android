package com.asfoundation.wallet.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.support.SupportNotificationProperties
import com.asfoundation.wallet.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : BaseActivity() {
  companion object {
    @JvmStatic
    fun newIntent(context: Context, fromSupportNotification: Boolean = false): Intent {
      val intent = Intent(context, OnboardingActivity::class.java)
      intent.putExtra(SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK,
          fromSupportNotification)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_onboarding)
  }
}