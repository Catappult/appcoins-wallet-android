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
    setNavigationGraph()
  }
  private fun setNavigationGraph() {
    val navHostFragment =
      supportFragmentManager.findFragmentById(R.id.nav_fragment_container) as NavHostFragment
    val navController = navHostFragment.navController

    val navGraph = navController.navInflater.inflate(R.navigation.onboarding_graph)
    navGraph.startDestination =
      if (intent.getBooleanExtra(FROM_IAP, false)) {
        R.id.onboarding_iap_fragment
      } else {
        R.id.onboarding_fragment
      }

    navController.graph = navGraph
  }

}