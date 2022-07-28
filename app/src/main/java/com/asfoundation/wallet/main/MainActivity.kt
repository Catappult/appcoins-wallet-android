package com.asfoundation.wallet.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.FirstInstallAnalytics
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.main.appsflyer.ApkOriginVerification
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Container activity for main screen with bottom navigation (Home, Promotions, My Wallets, Top up)
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
  SingleStateFragment<MainActivityState, MainActivitySideEffect> { //aaa123

  @Inject
  lateinit var navigator: MainActivityNavigator
  lateinit var navController: NavController

  @Inject
  lateinit var firstInstallAnalytics: FirstInstallAnalytics

  private val viewModel: MainActivityViewModel by viewModels()

  private lateinit var authenticationResultLauncher: ActivityResultLauncher<Intent>

  /**
  To avoid having to set the theme back to the main app one, we should use the new splash screen api.
  https://developer.android.com/guide/topics/ui/splash-screen
  We set the postSplashScreenTheme and this allows to delay the splash based on a condition if needed in the options inside installSplashScreen()
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.MaterialAppTheme)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)

    initNavController()

    if (savedInstanceState == null) {
      handleFirstRun()
    }
    handleAuthenticationResult()
    viewModel.collectStateAndEvents(lifecycle, lifecycleScope)
  }

  private fun initNavController() {
    val navHostFragment = supportFragmentManager.findFragmentById(
      R.id.full_host_container
    ) as NavHostFragment
    navController = navHostFragment.navController
  }

  private fun handleAuthenticationResult() {
    authenticationResultLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AuthenticationPromptActivity.RESULT_OK) {
          viewModel.handleInitialNavigation(authComplete = true)
        } else {
          finish()
        }
      }
  }

  private fun handleFirstRun() {
    val isFirstRun = getSharedPreferences("PREFERENCE", 0)
      .getBoolean("isFirstRun", true)
    if (isFirstRun) {
      firstInstallAnalytics.sendFirstInstallInfo()
      ApkOriginVerification(this)
      getSharedPreferences("PREFERENCE", 0)
        .edit()
        .putBoolean("isFirstRun", false)
        .apply()
    }
  }

  override fun onStateChanged(state: MainActivityState) = Unit

  override fun onSideEffect(sideEffect: MainActivitySideEffect) {
    when (sideEffect) {
      MainActivitySideEffect.NavigateToAutoUpdate -> navigator.navigateToAutoUpdate(navController)
      MainActivitySideEffect.NavigateToFingerprintAuthentication ->
        navigator.showAuthenticationActivity(this, authenticationResultLauncher)
      MainActivitySideEffect.NavigateToOnboarding -> navigator.navigateToOnboarding(
        navController
      )
      MainActivitySideEffect.NavigateToNavigationBar -> navigator.navigateToNavBarFragment(
        navController
      )
    }
  }

  companion object {
    fun newIntent(context: Context, supportNotificationClicked: Boolean): Intent {
      return Intent(context, MainActivity::class.java).apply {
        putExtra(SUPPORT_NOTIFICATION_CLICK, supportNotificationClicked)
      }
    }
  }
}