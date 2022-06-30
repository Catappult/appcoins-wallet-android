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
    private const val FROM_IAP = "from_iap"

    @JvmStatic
    fun newIntent(context: Context, fromIap : Boolean, fromSupportNotification: Boolean = false) =
      Intent(context, OnboardingActivity::class.java).apply {
        putExtra(SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK, fromSupportNotification)
        putExtra(FROM_IAP, fromIap)
      }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_onboarding)
  }
}