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
import com.appcoins.wallet.core.utils.android_common.Log
import com.appcoins.wallet.core.utils.android_common.NetworkMonitor
import com.appcoins.wallet.core.utils.android_common.OnNewIntentActivityHandler
import com.appcoins.wallet.core.utils.jvm_common.RxBus
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.main.nav_bar.NavBarFragment
import com.asfoundation.wallet.main.splash.bus.SplashFinishEvent
import com.asfoundation.wallet.onboarding.OnboardingFragment
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener
import com.asfoundation.wallet.onboarding_new_payment.payment_result.SdkPaymentWebSocketListener.Companion.SDK_STATUS_SUCCESS
import com.appcoins.wallet.core.analytics.analytics.notification.NotificationAnalytics
import com.asfoundation.wallet.firebase_messaging.PushNotificationProperties
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.login.LOGIN_CODE
import com.asfoundation.wallet.ui.login.RESPONSE_TOAST_MESSAGE
import com.asfoundation.wallet.ui.login.custom_tab_login.CustomTabLoginActivity.Companion.LOGIN_NOT_PROCESSED
import com.asfoundation.wallet.ui.login.usecases.FetchUserKeyUseCase.FetchUserKeyResult.Companion.ALREADY_LOGGED_IN_CODE
import com.asfoundation.wallet.ui.login.usecases.FetchUserKeyUseCase.FetchUserKeyResult.Companion.WALLET_SWITCHED_CODE
import com.asfoundation.wallet.verification.ui.paypal.VerificationPayPalProperties.PAYPAL_VERIFICATION_REQUIRED
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import io.sentry.android.fragment.SentryFragmentLifecycleCallbacks
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

/**
 * Container activity for main screen with bottom navigation (Home, Promotions, My Wallets, Top up)
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnNewIntentActivityHandler,
  SingleStateFragment<MainActivityState, MainActivitySideEffect> {

  @Inject
  lateinit var navigator: MainActivityNavigator

  @Inject
  lateinit var notificationAnalytics: NotificationAnalytics
  lateinit var navController: NavController

  @Inject
  lateinit var networkMonitor: NetworkMonitor

  private val viewModel: MainActivityViewModel by viewModels()

  private lateinit var authenticationResultLauncher: ActivityResultLauncher<Intent>

  private var disposable: Disposable? = null

  private var newIntent: Intent? = null

  /**
  To avoid having to set the theme back to the main app one, we should use the new splash screen api.
  https://developer.android.com/guide/topics/ui/splash-screen
  We set the postSplashScreenTheme and this allows to delay the splash based on a condition if needed in the options inside installSplashScreen()
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.MaterialAppTheme)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)

    supportFragmentManager.registerFragmentLifecycleCallbacks(
      SentryFragmentLifecycleCallbacks(), true
    )
    initNavController()
    handleSplashScreenResult()
    handleAuthenticationResult()
    handleNotificationClick(intent)
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

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    newIntent = intent
    handleSnackBarMessage(intent, viewModel)
    handleNotificationClick(intent)
    handleInitialNavigation(intent = intent, newIntent = true)
  }

  private fun handleNotificationClick(intent: Intent) {
    val notificationType = getNotificationTypeFromForeground(intent)
      ?: getNotificationTypeFromBackground(intent)
    notificationType?.let { notificationAnalytics.sendNotificationClickAnalytics(it) }
  }

  private fun getNotificationTypeFromForeground(intent: Intent): String? {
    if (!intent.hasExtra(NAV_DEEP_LINK_INTENT_KEY)) return null
    return intent.getStringExtra(PushNotificationProperties.NOTIFICATION_TYPE_KEY)
  }

  private fun getNotificationTypeFromBackground(intent: Intent): String? =
    intent.getStringExtra(PushNotificationProperties.NOTIFICATION_TYPE_KEY)

  /**
   * Helper function responsible for checking within the received [Intent]
   * if there's information regarding the snackBar, the stores it in the [MainActivityViewModel].
   *
   * @param intent The [Intent] to be checked.
   * @param viewModel The [MainActivityViewModel] to be updated.
   */
  private fun handleSnackBarMessage(intent: Intent, viewModel: MainActivityViewModel) {
    val snackBarCode = intent.getIntExtra(
      LOGIN_CODE,
      LOGIN_NOT_PROCESSED
    )
    val snackBarMessage = intent.getStringExtra(RESPONSE_TOAST_MESSAGE)
    val snackBar = when (snackBarCode) {
      WALLET_SWITCHED_CODE -> {
        snackBarMessage?.let {
          SnackBarMessage.WalletSwitched(it)
        } ?: run {
          Log.e(TAG, "handleSnackBarMessage: snackBarMessage is null")
          SnackBarMessage.ShowNoMessage
        }
      }

      ALREADY_LOGGED_IN_CODE -> {
        SnackBarMessage.WalletAlreadyAdded
      }

      else -> {
        SnackBarMessage.ShowNoMessage
      }
    }
    viewModel.emitSnackBarMessage(snackBar)
  }

  private fun handleInitialNavigation(
    authComplete: Boolean = false,
    intent: Intent,
    fromSplashScreen: Boolean = false,
    newIntent: Boolean = false,
  ) {
    val action = intent.action
    val launchedFromHistory = intent.flags.and(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0

    if (action == Intent.ACTION_VIEW && launchedFromHistory) {
      val host = intent.data?.host
      when (host) {
        BuildConfig.GIFT_CARD_HOST ->
          viewModel.handleInitialNavigation(
            authComplete = authComplete,
            giftCard = intent.data?.getQueryParameter(DEEPLINK_GIFT_CARD_QUERY_PARAM),
            fromSplashScreen = fromSplashScreen,
            newIntent = newIntent,
          )

        BuildConfig.PROMO_CODE_HOST ->
          viewModel.handleInitialNavigation(
            authComplete = authComplete,
            promoCode = intent.data?.getQueryParameter(DEEPLINK_PROMO_CODE_QUERY_PARAM),
            fromSplashScreen = fromSplashScreen,
            newIntent = newIntent,
          )

        else ->
          viewModel.handleInitialNavigation(authComplete = authComplete)
      }
    } else {
      viewModel.handleInitialNavigation(authComplete = authComplete)
    }
  }

  private fun launchedFromPromoCodeOrGiftCard() =
    intent.action == Intent.ACTION_VIEW && (intent.data?.host == BuildConfig.GIFT_CARD_HOST || intent.data?.host == BuildConfig.PROMO_CODE_HOST)

  override fun onStateChanged(state: MainActivityState) = Unit

  override fun onSideEffect(sideEffect: MainActivitySideEffect) {
    when (sideEffect) {
      MainActivitySideEffect.NavigateToAutoUpdate ->
        navigator.navigateToAutoUpdate(navController)

      MainActivitySideEffect.NavigateToFingerprintAuthentication ->
        navigator.showAuthenticationActivity(this, authenticationResultLauncher)

      MainActivitySideEffect.NavigateToOnboarding -> {
        navigator.navigateToOnboarding(
          navController = navController,
          createWalletAutomatically = launchedFromPromoCodeOrGiftCard()
        )
      }

      is MainActivitySideEffect.NavigateToOnboardingRecoverGuestWallet -> {
        val launchedFromPromoCodeOrGiftCard = launchedFromPromoCodeOrGiftCard()
        navigator.navigateToOnboardingRecoverGuestWallet(
          navController = navController,
          backupModel = sideEffect.backupModel,
          createWalletAutomatically = launchedFromPromoCodeOrGiftCard
        )
      }

      MainActivitySideEffect.NavigateToNavigationBar ->
        navigator.navigateToNavBarFragment(navController)

      MainActivitySideEffect.NavigateToPayPalVerification ->
        navigator.navigateToPayPalVerificationFragment(navController)

      is MainActivitySideEffect.NavigateToGiftCard ->
        if (navController.currentDestination?.id == R.id.nav_bar_fragment ||
          navController.currentDestination?.id == R.id.onboarding_fragment
        ) {
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

      is MainActivitySideEffect.NavigateToPromoCode ->
        if (navController.currentDestination?.id == R.id.nav_bar_fragment ||
          navController.currentDestination?.id == R.id.onboarding_fragment
        ) {
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
    val fragment = (supportFragmentManager.findFragmentById(R.id.main_host_container)
      ?.childFragmentManager
      ?.fragments
      ?.last())

    when (fragment) {
      is NavBarFragment -> fragment.handleGiftCard(giftCard)
      is OnboardingFragment -> fragment.createWalletAutomatically()
    }
  }

  override fun getCurrentIntent(): Intent =
    newIntent ?: super.getIntent()

  private fun setPromoCodeToCurrentFragment(promoCode: String) {
    val fragment = (supportFragmentManager.findFragmentById(R.id.main_host_container)
      ?.childFragmentManager
      ?.fragments
      ?.last())

    when (fragment) {
      is NavBarFragment -> fragment.handlePromoCode(promoCode)
      is OnboardingFragment -> fragment.createWalletAutomatically()
    }
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
    const val TAG = "MainActivity"
    private const val NAV_DEEP_LINK_INTENT_KEY = "android-support-nav:controller:deepLinkIntent"
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