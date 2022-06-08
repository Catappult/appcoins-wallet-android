package com.asfoundation.wallet.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.asf.wallet.R
import com.asfoundation.wallet.support.SupportNotificationProperties
import com.asfoundation.wallet.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : BaseActivity() {
  companion object {
    const val IS_FROM_IAP = "is_from_iap"
    @JvmStatic
    fun newIntent(context: Context, fromIap : Boolean, fromSupportNotification: Boolean = false): Intent {
      val intent = Intent(context, OnboardingActivity::class.java)
      intent.putExtra(SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK,
          fromSupportNotification)
      intent.putExtra(IS_FROM_IAP,fromIap)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_onboarding)
    setNavigationGraph()
  }
  private fun setNavigationGraph() {
    val navHostFragment =
      supportFragmentManager.findFragmentById(R.id.nav_fragment_container) as NavHostFragment
    val navController = navHostFragment.navController

    val navGraph = navController.navInflater.inflate(R.navigation.onboarding_graph)
    navGraph.startDestination =
      if (intent.getBooleanExtra(IS_FROM_IAP, false)) {
        R.id.onboarding_iap_fragment
      } else {
        R.id.onboarding_fragment
      }

    navController.graph = navGraph
  }

}