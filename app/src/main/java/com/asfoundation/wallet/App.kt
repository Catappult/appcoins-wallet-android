package com.asfoundation.wallet

import androidx.multidex.MultiDexApplication
import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards
import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import com.appcoins.wallet.billing.BillingDependenciesProvider
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.analytics.IndicativeAnalytics
import com.asfoundation.wallet.analytics.LaunchInteractor
import com.asfoundation.wallet.analytics.RakamAnalytics
import com.asfoundation.wallet.analytics.SentryAnalytics
import com.asfoundation.wallet.analytics.UxCamUtils
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.logging.FlurryReceiver
import com.asfoundation.wallet.poa.ProofOfAttentionService
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.support.AlarmManagerBroadcastReceiver
import com.asfoundation.wallet.ui.iab.AppcoinsOperationsDataSaver
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.flurry.android.FlurryAgent
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.HiltAndroidApp
import io.intercom.android.sdk.Intercom
import io.reactivex.Completable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Provider
import java.security.Security
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class App : MultiDexApplication(), BillingDependenciesProvider {

  @Inject
  lateinit var proofOfAttentionService: ProofOfAttentionService

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  lateinit var appcoinsOperationsDataSaver: AppcoinsOperationsDataSaver

  @Inject
  lateinit var brokerBdsApi: RemoteRepository.BrokerBdsApi

  @Inject
  lateinit var inappBdsApi: RemoteRepository.InappBdsApi

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
  lateinit var rakamAnalytics: RakamAnalytics

  @Inject
  lateinit var indicativeAnalytics: IndicativeAnalytics

  @Inject
  lateinit var sentryAnalytics: SentryAnalytics

  @Inject
  lateinit var preferencesRepositoryType: PreferencesRepositoryType

  @Inject
  lateinit var analyticsManager: AnalyticsManager

  @Inject
  lateinit var launchInteractor: LaunchInteractor

  @Inject
  lateinit var subscriptionBillingApi: SubscriptionBillingApi

  @Inject
  lateinit var billingSerializer: ExternalBillingSerializer

  @Inject
  lateinit var uxCamUtils: UxCamUtils

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
    proofOfAttentionService.start()
    appcoinsOperationsDataSaver.start()
    appcoinsRewards.start()
    initializeIndicative()
    initializeRakam()
    initiateIntercom()
    initiateSentry()
    initiateUxCam()
    setupBouncyCastle()
    initializeWalletId()
  }

  private fun initializeRakam() {
    rakamAnalytics.initialize()
        // Hacky way to wait for rakam initialization.. For some reason Rakam is initializing
        // in another thread internally, and there's no callback for us to wait for that
        .delay(3000, TimeUnit.MILLISECONDS)
        .andThen(Completable.fromAction {
          launchInteractor.sendFirstLaunchEvent()
        })
        .subscribeOn(Schedulers.io())
        .subscribe()
  }
  private fun initializeIndicative() {
    indicativeAnalytics.initialize()
      .subscribeOn(Schedulers.io())
      .subscribe()
  }

  private fun initiateUxCam() {
    uxCamUtils.initialize()?.subscribe()
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
    if (provider.equals(BouncyCastleProvider::class.java)) { return }
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

  private fun initiateSentry() {
    sentryAnalytics.initialize().subscribe()
  }

  private fun initiateIntercom() {
    Intercom.initialize(this, BuildConfig.INTERCOM_API_KEY, BuildConfig.INTERCOM_APP_ID)
    Intercom.client()
        .setInAppMessageVisibility(Intercom.Visibility.GONE)
  }

  private fun initializeWalletId() {
    if (preferencesRepositoryType.getWalletId() == null) {
      val id = UUID.randomUUID()
          .toString()
      preferencesRepositoryType.setWalletId(id)
    }
  }

  fun analyticsManager() = analyticsManager

  override fun supportedVersion() = BuildConfig.BILLING_SUPPORTED_VERSION

  override fun brokerBdsApi() = brokerBdsApi

  override fun inappBdsApi() = inappBdsApi

  override fun walletService() = walletService

  override fun proxyService() = proxyService

  override fun billingMessagesMapper() = billingMessagesMapper

  override fun bdsApiSecondary() = bdsapiSecondary

  override fun subscriptionBillingService() = subscriptionBillingApi

  override fun billingSerializer() = billingSerializer
}