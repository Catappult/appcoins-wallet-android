package com.asfoundation.wallet.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.support.SupportNotificationProperties
import com.asfoundation.wallet.ui.BaseActivity


class OnboardingActivityNew : BaseActivity() {
  companion object {
    @JvmStatic
    fun newIntent(context: Context, fromSupportNotification: Boolean = false): Intent {
      val intent = Intent(context, OnboardingActivityNew::class.java)
      intent.putExtra(SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK,
          fromSupportNotification)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_onboarding_new)

//    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_fragment_container) as NavHostFragment
//    val navController = navHostFragment.navController
//    navController.navigate(R.id.onboarding_fragment)
//    if (savedInstanceState == null) {
//      supportFragmentManager.beginTransaction()
//          .replace(R.id.fragment_container, OnboardingFragment.newInstance())
//          .commit()
//    }
  }
}