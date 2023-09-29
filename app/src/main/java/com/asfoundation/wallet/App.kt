package com.asfoundation.wallet

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.multidex.MultiDexApplication
import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards
import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.billing.BillingDependenciesProvider
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.core.analytics.analytics.SentryAnalytics
import com.appcoins.wallet.core.analytics.analytics.logging.FlurryReceiver
import com.appcoins.wallet.core.analytics.analytics.partners.OemIdExtractorService
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.base.MagnesUtils
import com.appcoins.wallet.core.network.bds.api.BdsApiSecondary
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.product.InappBillingApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.utils.properties.MiscProperties
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.analytics.InitilizeDataAnalytics
import com.asfoundation.wallet.app_start.AppStartProbe
import com.asfoundation.wallet.app_start.AppStartUseCase
import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.main.appsflyer.ApkOriginVerification
import com.asfoundation.wallet.support.AlarmManagerBroadcastReceiver
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.flurry.android.FlurryAgent
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.HiltAndroidApp
import io.intercom.android.sdk.Intercom
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Provider
import java.security.Security
import java.util.UUID
import javax.inject.Inject


@HiltAndroidApp
class App : MultiDexApplication(), BillingDependenciesProvider {

  @Inject
  lateinit var appStartUseCase: AppStartUseCase

  @Inject
  lateinit var appStartProbe: AppStartProbe

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  lateinit var appcoinsOperationsDataSaver: AppcoinsOperationsDataSaver

  @Inject
  lateinit var brokerBdsApi: BrokerBdsApi

  @Inject
  lateinit var inappApi: InappBillingApi

  @Inject
  lateinit var walletService: WalletService

  @Inject
  lateinit var proxyService: ProxyService

  @Inject
  lateinit var appcoinsRewards: AppcoinsRewards

  @Inject
  lateinit var billingMessagesMapper: BillingMessagesMapper

  @Inject
  lateinit var bdsapiSecondary: BdsApiSecondary

  @Inject
  lateinit var idsRepository: IdsRepository

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var initilizeDataAnalytics: InitilizeDataAnalytics

  @Inject
  lateinit var sentryAnalytics: SentryAnalytics

  @Inject
  lateinit var analyticsManager: AnalyticsManager

  @Inject
  lateinit var commonsPreferencesDataSource: CommonsPreferencesDataSource

  @Inject
  lateinit var subscriptionBillingApi: SubscriptionBillingApi

  @Inject
  lateinit var rxSchedulers: RxSchedulers

  @Inject
  lateinit var ewtObtainer: EwtAuthenticatorService

  @Inject
  lateinit var oemIdExtractorService: OemIdExtractorService

  companion object {
    private val TAG = App::class.java.name
  }

  override fun onCreate() {
    super.onCreate()
    setupRxJava()
    val gpsAvailable = checkGooglePlayServices()
    if (gpsAvailable.not()) setupSupportNotificationAlarm()
    initiateFlurry()
    inAppPurchaseInteractor.start()
    appcoinsOperationsDataSaver.start()
    appcoinsRewards.start()
    initializeIndicative()
    initiateIntercom()
    initializeSentry()
    initializeMagnes()
    setupBouncyCastle()
    initializeWalletId()
    MainScope().launch {
      val mode = appStartUseCase.startModes.first()
      // OSP GP: Add enough delay to let wallet be created and set as user ID to the Analytics
      // Should be refactored in order to remove the fixed delay
      Handler(Looper.getMainLooper()).postDelayed({ appStartProbe(mode) }, 3000)
      if (mode != StartMode.Subsequent) ApkOriginVerification(applicationContext)
    }
    registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
      private var runningCount = 0
      override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
          if (runningCount++ == 0) appStartUseCase.registerAppStart()
        }
      }

      override fun onActivityStarted(activity: Activity) {}
      override fun onActivityResumed(activity: Activity) {}
      override fun onActivityPaused(activity: Activity) {}
      override fun onActivityStopped(activity: Activity) {}
      override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
      override fun onActivityDestroyed(activity: Activity) {
        if (activity.isChangingConfigurations.not()) runningCount--
      }
    })
  }

  private fun initializeIndicative() {
    initilizeDataAnalytics.initializeIndicative()
      .subscribeOn(Schedulers.io())
      .subscribe()
  }

  private fun initializeSentry() {
    initilizeDataAnalytics.initializeSentry().subscribe()
  }

  private fun setupRxJava() {
    RxJavaPlugins.setErrorHandler { throwable: Throwable ->
      if (throwable is UndeliverableException) {
        if (BuildConfig.DEBUG) {
          throwable.printStackTrace()
        } else {
          logger.log(TAG, throwable)
        }
      } else {
        throw RuntimeException(throwable)
      }
    }
  }

  // fixes issue with web3j to support ECDSA
  // https://github.com/web3j/web3j/issues/915
  private fun setupBouncyCastle() {
    val provider: Provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) ?: return
    if (provider.equals(BouncyCastleProvider::class.java)) {
      return
    }
    Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
    Security.insertProviderAt(BouncyCastleProvider(), 1)
  }

  private fun checkGooglePlayServices(): Boolean {
    val availability = GoogleApiAvailability.getInstance()
    return availability.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
  }

  private fun setupSupportNotificationAlarm() {
    AlarmManagerBroadcastReceiver.scheduleAlarm(this)
  }

  private fun initiateFlurry() {
    if (!BuildConfig.DEBUG) {
      FlurryAgent.Builder()
        .withLogEnabled(false)
        .build(this, BuildConfig.FLURRY_APK_KEY)
      logger.addReceiver(FlurryReceiver())
    }
  }

  private fun initiateIntercom() {
    Intercom.initialize(this, BuildConfig.INTERCOM_API_KEY, BuildConfig.INTERCOM_APP_ID)
    Intercom.client()
      .setInAppMessageVisibility(Intercom.Visibility.GONE)
  }

  private fun initializeWalletId() {
    if (commonsPreferencesDataSource.getWalletId() == null) {
      val id = UUID.randomUUID()
        .toString()
      commonsPreferencesDataSource.setWalletId(id)
    }
  }

  private fun initializeMagnes() {
    MagnesUtils.start(this)
    MagnesUtils.collectAndSubmit(this)
  }

  fun analyticsManager() = analyticsManager

  override fun supportedVersion() = MiscProperties.BILLING_SUPPORTED_VERSION

  override fun brokerBdsApi() = brokerBdsApi

  override fun inappApi() = inappApi

  override fun walletService() = walletService

  override fun proxyService() = proxyService

  override fun billingMessagesMapper() = billingMessagesMapper

  override fun bdsApiSecondary() = bdsapiSecondary

  override fun subscriptionsApi() = subscriptionBillingApi

  override fun rxSchedulers() = rxSchedulers

  override fun ewtObtainer() = ewtObtainer

  override fun oemIdExtractorService() = oemIdExtractorService

}