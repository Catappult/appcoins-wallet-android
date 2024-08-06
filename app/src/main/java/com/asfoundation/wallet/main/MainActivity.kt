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
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.utils.android_common.NetworkMonitor
import com.appcoins.wallet.core.utils.jvm_common.RxBus
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.main.nav_bar.NavBarFragment
import com.asfoundation.wallet.main.splash.bus.SplashFinishEvent
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener.Companion.SDK_STATUS_SUCCESS
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.verification.ui.paypal.VerificationPayPalProperties.PAYPAL_VERIFICATION_REQUIRED
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import okhttp3.Request
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * Container activity for main screen with bottom navigation (Home, Promotions, My Wallets, Top up)
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
  SingleStateFragment<MainActivityState, MainActivitySideEffect> {

  @Inject
  lateinit var navigator: MainActivityNavigator
  lateinit var navController: NavController

  @Inject
  lateinit var networkMonitor: NetworkMonitor

  private val viewModel: MainActivityViewModel by viewModels()

  private lateinit var authenticationResultLauncher: ActivityResultLauncher<Intent>

  private var disposable: Disposable? = null

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
    handleSplashScreenResult()
    handleAuthenticationResult()
    viewModel.collectStateAndEvents(lifecycle, lifecycleScope)
  }

  private fun initNavController() {
    val navHostFragment = supportFragmentManager.findFragmentById(
      R.id.main_host_container
    ) as NavHostFragment
    navController = navHostFragment.navController
  }

  private fun handleSplashScreenResult() {
    disposable = RxBus.listen(SplashFinishEvent().javaClass).subscribe {
      handleInitialNavigation(intent = intent, fromSplashScreen = true)
    }
  }

  private fun handleAuthenticationResult() {
    authenticationResultLauncher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AuthenticationPromptActivity.RESULT_OK) {
          handleInitialNavigation(authComplete = true, intent = intent)
        } else {
          finish()
        }
      }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    handleInitialNavigation(intent = intent)
  }

  private fun handleInitialNavigation(authComplete: Boolean = false, intent: Intent? = null, fromSplashScreen: Boolean = false) {
    val action = intent?.action
    val launchedFromHistory = intent?.flags?.and(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0

    if (action == Intent.ACTION_VIEW && launchedFromHistory) {
      val host = intent.data?.host
      when (host) {
        BuildConfig.GIFT_CARD_HOST ->
          viewModel.handleInitialNavigation(
            authComplete = authComplete,
            giftCard = intent.data?.getQueryParameter(DEEPLINK_GIFT_CARD_QUERY_PARAM),
            fromSplashScreen = fromSplashScreen
          )

        BuildConfig.PROMO_CODE_HOST ->
          viewModel.handleInitialNavigation(
            authComplete = authComplete,
            promoCode = intent.data?.getQueryParameter(DEEPLINK_PROMO_CODE_QUERY_PARAM),
            fromSplashScreen = fromSplashScreen
          )

        else ->
          viewModel.handleInitialNavigation(authComplete = authComplete)
      }
    } else {
      viewModel.handleInitialNavigation(authComplete = authComplete)
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

      is MainActivitySideEffect.NavigateToOnboardingRecoverGuestWallet ->
        navigator.navigateToOnboardingRecoverGuestWallet(
          navController,
          sideEffect.backup,
          sideEffect.flow
        )

      MainActivitySideEffect.NavigateToNavigationBar -> navigator.navigateToNavBarFragment(
        navController
      )

      MainActivitySideEffect.NavigateToPayPalVerification -> navigator.navigateToPayPalVerificationFragment(
        navController
      )

      is MainActivitySideEffect.NavigateToGiftCard -> {
        if (navController.currentDestination?.id == R.id.nav_bar_fragment) {
          setGiftCardToCurrentFragment(sideEffect.giftCard)
        } else {
          if (sideEffect.fromSplashScreen) {
            navigator.navigateToGiftCardFromSplashScreen(
              navController = navController,
              giftCard = sideEffect.giftCard
            )
          } else {
            navigator.navigateToGiftCard(
              navController = navController,
              giftCard = sideEffect.giftCard
            )
          }
        }
      }

      is MainActivitySideEffect.NavigateToPromoCode -> if (navController.currentDestination?.id == R.id.nav_bar_fragment) {
        setPromoCodeToCurrentFragment(sideEffect.promoCode)
      } else {
        if (sideEffect.fromSplashScreen) {
          navigator.navigateToPromoCodeFromSplashScreen(
            navController = navController,
            promoCode = sideEffect.promoCode
          )
        } else {
          navigator.navigateToPromoCode(
            navController = navController,
            promoCode = sideEffect.promoCode
          )
        }
      }
    }
  }

  private fun setGiftCardToCurrentFragment(giftCard: String) {
    (supportFragmentManager.findFragmentById(R.id.main_host_container)
      ?.childFragmentManager
      ?.fragments
      ?.last() as? NavBarFragment
    )?.handleGiftCard(giftCard)
  }

  private fun setPromoCodeToCurrentFragment(promoCode: String) {
    (supportFragmentManager.findFragmentById(R.id.main_host_container)
      ?.childFragmentManager
      ?.fragments
      ?.last() as? NavBarFragment
    )?.handlePromoCode(promoCode)
  }

  override fun onDestroy() {
    val responseCode = viewModel.getResponseCodeWebSocket()
    if (viewModel.isOnboardingPaymentFlow && responseCode != SDK_STATUS_SUCCESS) {
      val request =
        Request.Builder().url("ws://localhost:".plus(viewModel.getWsPort())).build()
      val listener = SdkPaymentWebSocketListener("", "", responseCode)
      OkHttpClient().newWebSocket(request, listener)
    }
    disposable?.dispose()
    super.onDestroy()
  }


  companion object {
    private const val DEEPLINK_GIFT_CARD_QUERY_PARAM = "giftcard"
    private const val DEEPLINK_PROMO_CODE_QUERY_PARAM = "promocode"

    fun newIntent(
      context: Context,
      supportNotificationClicked: Boolean,
      isPayPalVerificationRequired: Boolean = false
    ): Intent {
      return Intent(context, MainActivity::class.java).apply {
        putExtra(SUPPORT_NOTIFICATION_CLICK, supportNotificationClicked)
        putExtra(PAYPAL_VERIFICATION_REQUIRED, isPayPalVerificationRequired)
      }
    }
  }
}