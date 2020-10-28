package com.asfoundation.wallet.di

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import androidx.room.Room
import cm.aptoide.analytics.AnalyticsManager
import com.adyen.checkout.core.api.Environment
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards
import com.appcoins.wallet.appcoins.rewards.repository.BdsAppcoinsRewardsRepository
import com.appcoins.wallet.appcoins.rewards.repository.backend.BackendApi
import com.appcoins.wallet.bdsbilling.*
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmissionImpl
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository.BdsApi
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.commons.MemoryCache
import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.PromotionDatabase
import com.appcoins.wallet.gamification.repository.PromotionDatabase.Companion.MIGRATION_1_2
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.permissions.Permissions
import com.aptoide.apk.injector.extractor.data.Extractor
import com.aptoide.apk.injector.extractor.data.ExtractorV1
import com.aptoide.apk.injector.extractor.data.ExtractorV2
import com.aptoide.apk.injector.extractor.domain.IExtract
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxyBuilder
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxySdk
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.App
import com.asfoundation.wallet.C
import com.asfoundation.wallet.advertise.PoaAnalyticsController
import com.asfoundation.wallet.analytics.*
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import com.asfoundation.wallet.billing.CreditsRemoteRepository
import com.asfoundation.wallet.billing.analytics.*
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.interact.BalanceGetter
import com.asfoundation.wallet.interact.BuildConfigDefaultTokenProvider
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.logging.DebugReceiver
import com.asfoundation.wallet.logging.LogReceiver
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.logging.WalletLogger
import com.asfoundation.wallet.permissions.repository.PermissionRepository
import com.asfoundation.wallet.permissions.repository.PermissionsDatabase
import com.asfoundation.wallet.poa.*
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.repository.IpCountryCodeProvider.IpApi
import com.asfoundation.wallet.router.GasSettingsRouter
import com.asfoundation.wallet.service.AutoUpdateService.AutoUpdateApi
import com.asfoundation.wallet.service.CampaignService
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.asfoundation.wallet.service.TokenRateService
import com.asfoundation.wallet.support.SupportSharedPreferences
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.topup.TopUpValuesApiResponseMapper
import com.asfoundation.wallet.transactions.TransactionsAnalytics
import com.asfoundation.wallet.transactions.TransactionsMapper
import com.asfoundation.wallet.ui.airdrop.AirdropChainIdMapper
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.iab.*
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer
import com.asfoundation.wallet.ui.iab.raiden.NonceObtainerFactory
import com.asfoundation.wallet.ui.iab.raiden.Web3jNonceProvider
import com.asfoundation.wallet.util.*
import com.asfoundation.wallet.util.CurrencyFormatUtils.Companion.create
import com.asfoundation.wallet.wallet_validation.generic.WalletValidationAnalytics
import com.facebook.appevents.AppEventsLogger
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
internal class AppModule {
  @Provides
  fun provideContext(application: App): Context = application.applicationContext

  @Singleton
  @Provides
  fun provideGson() = Gson()

  @Singleton
  @Provides
  @Named("blockchain")
  fun provideBlockchainOkHttpClient(context: Context,
                                    preferencesRepositoryType: PreferencesRepositoryType): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor(context, preferencesRepositoryType))
        .addInterceptor(LogInterceptor())
        .connectTimeout(15, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.MINUTES)
        .writeTimeout(30, TimeUnit.MINUTES)
        .build()
  }

  @Singleton
  @Provides
  @Named("default")
  fun provideDefaultOkHttpClient(context: Context,
                                 preferencesRepositoryType: PreferencesRepositoryType): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor(context, preferencesRepositoryType))
        .addInterceptor(LogInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()
  }

  @Singleton
  @Provides
  fun passwordStore(context: Context, logger: Logger): PasswordStore {
    return TrustPasswordStore(context, logger)
  }

  @Singleton
  @Provides
  fun provideLogger(): Logger {
    val receivers = ArrayList<LogReceiver>()
    if (BuildConfig.DEBUG) {
      receivers.add(DebugReceiver())
    }
    return WalletLogger(receivers)
  }

  @Singleton
  @Provides
  fun providesBillingPaymentProofSubmission(api: BdsApi,
                                            walletService: WalletService,
                                            bdsApi: BdsApiSecondary): BillingPaymentProofSubmission {
    return BillingPaymentProofSubmissionImpl.Builder()
        .setApi(api)
        .setBdsApiSecondary(bdsApi)
        .setWalletService(walletService)
        .build()
  }

  @Singleton
  @Provides
  fun provideErrorMapper() = ErrorMapper()

  @Provides
  fun provideGasSettingsRouter() = GasSettingsRouter()

  @Provides
  fun provideLocalPaymentAnalytics(billingAnalytics: BillingAnalytics,
                                   inAppPurchaseInteractor: InAppPurchaseInteractor): LocalPaymentAnalytics {
    return LocalPaymentAnalytics(billingAnalytics, inAppPurchaseInteractor, Schedulers.io())
  }

  @Provides
  fun providePaymentMethodsMapper() = PaymentMethodsMapper()

  @Provides
  fun provideNonceObtainer(web3jProvider: Web3jProvider): MultiWalletNonceObtainer {
    return MultiWalletNonceObtainer(NonceObtainerFactory(30000, Web3jNonceProvider(web3jProvider)))
  }

  @Provides
  fun provideEIPTransferParser(defaultTokenProvider: DefaultTokenProvider): EIPTransactionParser {
    return EIPTransactionParser(defaultTokenProvider)
  }

  @Provides
  fun provideOneStepTransferParser(proxyService: ProxyService,
                                   billing: Billing, tokenRateService: TokenRateService,
                                   defaultTokenProvider: DefaultTokenProvider): OneStepTransactionParser {
    return OneStepTransactionParser(proxyService, billing, tokenRateService,
        MemoryCache(BehaviorSubject.create(), HashMap()), defaultTokenProvider)
  }

  @Provides
  fun provideTransferParser(eipTransactionParser: EIPTransactionParser,
                            oneStepTransactionParser: OneStepTransactionParser): TransferParser {
    return TransferParser(eipTransactionParser, oneStepTransactionParser)
  }

  @Provides
  fun provideDefaultTokenProvider(findDefaultWalletInteract: FindDefaultWalletInteract,
                                  networkInfo: NetworkInfo): DefaultTokenProvider {
    return BuildConfigDefaultTokenProvider(findDefaultWalletInteract, networkInfo)
  }

  @Singleton
  @Provides
  fun provideMessageDigest() = Calculator()

  @Singleton
  @Provides
  fun provideDataMapper() = DataMapper()

  @Singleton
  @Provides
  @Named("REGISTER_PROOF_GAS_LIMIT")
  fun provideRegisterPoaGasLimit() = BigDecimal(BuildConfig.REGISTER_PROOF_GAS_LIMIT)

  @Singleton
  @Provides
  fun provideBdsBackEndWriter(defaultWalletInteract: FindDefaultWalletInteract,
                              campaignService: CampaignService): ProofWriter {
    return BdsBackEndWriter(defaultWalletInteract, campaignService)
  }

  @Singleton
  @Provides
  fun provideAdsContractAddressSdk(): AppCoinsAddressProxySdk =
      AppCoinsAddressProxyBuilder().createAddressProxySdk()

  @Singleton
  @Provides
  fun provideHashCalculator(calculator: Calculator) =
      HashCalculator(BuildConfig.LEADING_ZEROS_ON_PROOF_OF_ATTENTION, calculator)

  @Provides
  @Named("MAX_NUMBER_PROOF_COMPONENTS")
  fun provideMaxNumberProofComponents() = 12

  @Provides
  fun provideTaggedCompositeDisposable() = TaggedCompositeDisposable(HashMap())

  @Provides
  @Singleton
  fun providesCountryCodeProvider(@Named("default") client: OkHttpClient,
                                  gson: Gson): CountryCodeProvider {
    val api = Retrofit.Builder()
        .baseUrl(IpCountryCodeProvider.ENDPOINT)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(IpApi::class.java)
    return IpCountryCodeProvider(api)
  }

  @Provides
  @Singleton
  fun provideInAppPurchaseDataSaver(context: Context, operationSources: OperationSources,
                                    appCoinsOperationRepository: AppCoinsOperationRepository): AppcoinsOperationsDataSaver {
    return AppcoinsOperationsDataSaver(operationSources.sources, appCoinsOperationRepository,
        AppInfoProvider(context, ImageSaver(context.filesDir
            .toString() + "/app_icons/")),
        Schedulers.io(), CompositeDisposable())
  }

  @Provides
  fun provideOperationSources(inAppPurchaseInteractor: InAppPurchaseInteractor,
                              proofOfAttentionService: ProofOfAttentionService): OperationSources {
    return OperationSources(inAppPurchaseInteractor, proofOfAttentionService)
  }

  @Provides
  fun provideAirdropChainIdMapper(networkInfo: NetworkInfo): AirdropChainIdMapper {
    return AirdropChainIdMapper(networkInfo)
  }

  @Singleton
  @Provides
  fun provideBillingFactory(walletService: WalletService, bdsRepository: BdsRepository): Billing {
    return BdsBilling(bdsRepository, walletService, BillingThrowableCodeMapper())
  }

  @Singleton
  @Provides
  fun provideAppcoinsRewards(walletService: WalletService, billing: Billing, backendApi: BackendApi,
                             remoteRepository: RemoteRepository): AppcoinsRewards {
    return AppcoinsRewards(
        BdsAppcoinsRewardsRepository(CreditsRemoteRepository(backendApi, remoteRepository)),
        object : com.appcoins.wallet.appcoins.rewards.repository.WalletService {
          override fun getWalletAddress() = walletService.getWalletAddress()

          override fun signContent(content: String) = walletService.signContent(content)
        }, MemoryCache(BehaviorSubject.create(), ConcurrentHashMap()), Schedulers.io(), billing,
        com.appcoins.wallet.appcoins.rewards.ErrorMapper())
  }

  @Singleton
  @Provides
  fun provideRewardsManager(appcoinsRewards: AppcoinsRewards, billing: Billing,
                            addressService: AddressService): RewardsManager {
    return RewardsManager(appcoinsRewards, billing, addressService)
  }

  @Singleton
  @Provides
  fun provideBillingMessagesMapper() = BillingMessagesMapper(ExternalBillingSerializer())

  @Provides
  fun provideAdyenEnvironment(): Environment {
    return if (BuildConfig.DEBUG) {
      Environment.TEST
    } else {
      Environment.EUROPE
    }
  }

  @Singleton
  @Provides
  fun provideSharedPreferences(context: Context): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(context)
  }

  @Provides
  fun provideGamification(promotionsRepository: PromotionsRepository) =
      Gamification(promotionsRepository)

  @Singleton
  @Provides
  fun provideBalanceGetter(appcoinsRewards: AppcoinsRewards): BalanceGetter {
    return object : BalanceGetter {
      override fun getBalance(address: String): Single<BigDecimal> {
        return appcoinsRewards.getBalance(address)
            .subscribeOn(Schedulers.io())
      }

      override fun getBalance(): Single<BigDecimal> {
        return Single.just(BigDecimal.ZERO)
      }
    }
  }

  @Singleton
  @Provides
  @Named("bi_event_list")
  fun provideBiEventList() = listOf(
      BillingAnalytics.PURCHASE_DETAILS,
      BillingAnalytics.PAYMENT_METHOD_DETAILS,
      BillingAnalytics.PAYMENT,
      PoaAnalytics.POA_STARTED,
      PoaAnalytics.POA_COMPLETED)

  @Singleton
  @Provides
  @Named("facebook_event_list")
  fun provideFacebookEventList() = listOf(
      BillingAnalytics.PURCHASE_DETAILS,
      BillingAnalytics.PAYMENT_METHOD_DETAILS,
      BillingAnalytics.PAYMENT,
      BillingAnalytics.REVENUE,
      PoaAnalytics.POA_STARTED,
      PoaAnalytics.POA_COMPLETED,
      TransactionsAnalytics.OPEN_APPLICATION,
      GamificationAnalytics.GAMIFICATION,
      GamificationAnalytics.GAMIFICATION_MORE_INFO
  )

  @Singleton
  @Provides
  @Named("rakam_event_list")
  fun provideRakamEventList() = listOf(
      BillingAnalytics.RAKAM_PRESELECTED_PAYMENT_METHOD,
      BillingAnalytics.RAKAM_PAYMENT_METHOD,
      BillingAnalytics.RAKAM_PAYMENT_CONFIRMATION,
      BillingAnalytics.RAKAM_PAYMENT_CONCLUSION,
      BillingAnalytics.RAKAM_PAYMENT_START,
      BillingAnalytics.RAKAM_PAYPAL_URL,
      TopUpAnalytics.WALLET_TOP_UP_START,
      TopUpAnalytics.WALLET_TOP_UP_SELECTION,
      TopUpAnalytics.WALLET_TOP_UP_CONFIRMATION,
      TopUpAnalytics.WALLET_TOP_UP_CONCLUSION,
      TopUpAnalytics.WALLET_TOP_UP_PAYPAL_URL,
      PoaAnalytics.RAKAM_POA_EVENT,
      WalletValidationAnalytics.WALLET_PHONE_NUMBER_VERIFICATION,
      WalletValidationAnalytics.WALLET_CODE_VERIFICATION,
      WalletValidationAnalytics.WALLET_VERIFICATION_CONFIRMATION,
      WalletsAnalytics.WALLET_CREATE_BACKUP,
      WalletsAnalytics.WALLET_SAVE_BACKUP,
      WalletsAnalytics.WALLET_CONFIRMATION_BACKUP,
      WalletsAnalytics.WALLET_SAVE_FILE,
      WalletsAnalytics.WALLET_IMPORT_RESTORE,
      WalletsAnalytics.WALLET_PASSWORD_RESTORE,
      PageViewAnalytics.WALLET_PAGE_VIEW
  )

  @Singleton
  @Provides
  @Named("amplitude_event_list")
  fun provideAmplitudeEventList() = listOf(
      BillingAnalytics.RAKAM_PRESELECTED_PAYMENT_METHOD,
      BillingAnalytics.RAKAM_PAYMENT_METHOD,
      BillingAnalytics.RAKAM_PAYMENT_CONFIRMATION,
      BillingAnalytics.RAKAM_PAYMENT_CONCLUSION,
      BillingAnalytics.RAKAM_PAYMENT_START,
      BillingAnalytics.RAKAM_PAYPAL_URL,
      TopUpAnalytics.WALLET_TOP_UP_START,
      TopUpAnalytics.WALLET_TOP_UP_SELECTION,
      TopUpAnalytics.WALLET_TOP_UP_CONFIRMATION,
      TopUpAnalytics.WALLET_TOP_UP_CONCLUSION,
      TopUpAnalytics.WALLET_TOP_UP_PAYPAL_URL,
      PoaAnalytics.RAKAM_POA_EVENT,
      WalletValidationAnalytics.WALLET_PHONE_NUMBER_VERIFICATION,
      WalletValidationAnalytics.WALLET_CODE_VERIFICATION,
      WalletValidationAnalytics.WALLET_VERIFICATION_CONFIRMATION,
      WalletsAnalytics.WALLET_CREATE_BACKUP,
      WalletsAnalytics.WALLET_SAVE_BACKUP,
      WalletsAnalytics.WALLET_CONFIRMATION_BACKUP,
      WalletsAnalytics.WALLET_SAVE_FILE,
      WalletsAnalytics.WALLET_IMPORT_RESTORE,
      WalletsAnalytics.WALLET_PASSWORD_RESTORE,
      PageViewAnalytics.WALLET_PAGE_VIEW
  )

  @Singleton
  @Provides
  fun provideAnalyticsManager(@Named("default") okHttpClient: OkHttpClient, api: AnalyticsAPI,
                              context: Context, @Named("bi_event_list") biEventList: List<String>,
                              @Named("facebook_event_list") facebookEventList: List<String>,
                              @Named("rakam_event_list") rakamEventList: List<String>,
                              @Named("amplitude_event_list")
                              amplitudeEventList: List<String>): AnalyticsManager {
    return AnalyticsManager.Builder()
        .addLogger(BackendEventLogger(api), biEventList)
        .addLogger(FacebookEventLogger(AppEventsLogger.newLogger(context)), facebookEventList)
        .addLogger(RakamEventLogger(), rakamEventList)
        .addLogger(AmplitudeEventLogger(), amplitudeEventList)
        .setAnalyticsNormalizer(KeysNormalizer())
        .setDebugLogger(LogcatAnalyticsLogger())
        .setKnockLogger(HttpClientKnockLogger(okHttpClient))
        .build()
  }

  @Singleton
  @Provides
  fun provideWalletEventSender(analytics: AnalyticsManager): WalletsEventSender =
      WalletsAnalytics(analytics)

  @Singleton
  @Provides
  fun provideBillingAnalytics(analytics: AnalyticsManager) = BillingAnalytics(analytics)

  @Singleton
  @Provides
  fun providePoAAnalytics(analytics: AnalyticsManager) = PoaAnalytics(analytics)

  @Singleton
  @Provides
  fun providesPoaAnalyticsController() = PoaAnalyticsController(CopyOnWriteArrayList())

  @Singleton
  @Provides
  fun providesPermissions(context: Context): Permissions {
    return Permissions(PermissionRepository(Room.databaseBuilder(context.applicationContext,
        PermissionsDatabase::class.java,
        "permissions_database")
        .build()
        .permissionsDao()))
  }

  @Singleton
  @Provides
  fun providesPromotionDatabase(context: Context): PromotionDatabase {
    return Room.databaseBuilder(context, PromotionDatabase::class.java, "promotion_database")
        .addMigrations(MIGRATION_1_2)
        .build()
  }

  @Singleton
  @Provides
  fun providesPromotionDao(promotionDatabase: PromotionDatabase) =
      promotionDatabase.promotionDao()

  @Singleton
  @Provides
  fun providesLevelsDao(promotionDatabase: PromotionDatabase) =
      promotionDatabase.levelsDao()

  @Singleton
  @Provides
  fun providesLevelDao(promotionDatabase: PromotionDatabase) =
      promotionDatabase.levelDao()

  @Provides
  fun providesObjectMapper(): ObjectMapper {
    return ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  }

  @Provides
  fun providesTopUpValuesApiResponseMapper() = TopUpValuesApiResponseMapper()

  @Singleton
  @Provides
  fun providesTransactionsAnalytics(analytics: AnalyticsManager) = TransactionsAnalytics(analytics)

  @Singleton
  @Provides
  fun provideGamificationAnalytics(analytics: AnalyticsManager) = GamificationAnalytics(analytics)

  @Provides
  fun providesOffChainTransactions(repository: OffChainTransactionsRepository,
                                   mapper: TransactionsMapper): OffChainTransactions {
    return OffChainTransactions(repository, versionCode)
  }

  private val versionCode: String
    get() = BuildConfig.VERSION_CODE.toString()

  @Provides
  fun provideTransactionsMapper() = TransactionsMapper()

  @Singleton
  @Provides
  fun provideNotificationManager(context: Context): NotificationManager {
    return context.applicationContext.getSystemService(
        Context.NOTIFICATION_SERVICE) as NotificationManager
  }

  @Singleton
  @Provides
  @Named("heads_up")
  fun provideHeadsUpNotificationBuilder(context: Context,
                                        notificationManager: NotificationManager): NotificationCompat.Builder {
    val builder: NotificationCompat.Builder
    val channelId = "notification_channel_heads_up_id"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channelName: CharSequence = "Notification channel"
      val importance = NotificationManager.IMPORTANCE_HIGH
      val notificationChannel =
          NotificationChannel(channelId, channelName, importance)
      builder = NotificationCompat.Builder(context, channelId)
      notificationManager.createNotificationChannel(notificationChannel)
    } else {
      builder = NotificationCompat.Builder(context, channelId)
      builder.setVibrate(LongArray(0))
    }
    return builder.setContentTitle(context.getString(R.string.app_name))
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setAutoCancel(true)
  }

  @Singleton
  @Provides
  fun provideIExtract(): IExtract {
    return Extractor(ExtractorV1(), ExtractorV2())
  }

  @Singleton
  @Provides
  fun providePackageManager(context: Context): PackageManager = context.packageManager

  @Singleton
  @Provides
  fun provideAutoUpdateApi(@Named("default") client: OkHttpClient, gson: Gson): AutoUpdateApi {
    val baseUrl = BuildConfig.BACKEND_HOST
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(AutoUpdateApi::class.java)
  }

  @Provides
  @Named("local_version_code")
  fun provideLocalVersionCode(context: Context, packageManager: PackageManager): Int {
    return try {
      packageManager.getPackageInfo(context.packageName, 0)
          .versionCode
    } catch (e: PackageManager.NameNotFoundException) {
      -1
    }
  }

  @Singleton
  @Provides
  fun provideSupportSharedPreferences(preferences: SharedPreferences) =
      SupportSharedPreferences(preferences)

  @Singleton
  @Provides
  fun provideRakamAnalyticsSetup(context: Context, idsRepository: IdsRepository,
                                 logger: Logger): RakamAnalytics {
    return RakamAnalytics(context, idsRepository, logger)
  }

  @Singleton
  @Provides
  fun provideAmplitudeAnalytics(context: Context,
                                idsRepository: IdsRepository): AmplitudeAnalytics {
    return AmplitudeAnalytics(context, idsRepository)
  }

  @Singleton
  @Provides
  fun provideTopUpAnalytics(analyticsManager: AnalyticsManager) = TopUpAnalytics(analyticsManager)

  @Singleton
  @Provides
  fun provideCurrencyFormatUtils() = create()

  @Singleton
  @Provides
  fun provideWalletValidationAnalytics(analyticsManager: AnalyticsManager) =
      WalletValidationAnalytics(analyticsManager)

  @Provides
  fun provideContentResolver(context: Context): ContentResolver = context.contentResolver

  @Singleton
  @Provides
  fun providesWeb3jProvider(@Named("blockchain") client: OkHttpClient,
                            networkInfo: NetworkInfo): Web3jProvider {
    return Web3jProvider(client, networkInfo)
  }

  @Singleton
  @Provides
  fun providesDefaultNetwork(): NetworkInfo {
    return if (BuildConfig.DEBUG) {
      NetworkInfo(C.ROPSTEN_NETWORK_NAME, C.ETH_SYMBOL,
          "https://ropsten.infura.io/v3/${BuildConfig.INFURA_API_KEY_ROPSTEN}",
          "https://ropsten.trustwalletapp.com/", "https://ropsten.etherscan.io/tx/", 3, false)
    } else {
      NetworkInfo(C.ETHEREUM_NETWORK_NAME, C.ETH_SYMBOL,
          "https://mainnet.infura.io/v3/${BuildConfig.INFURA_API_KEY_MAIN}",
          "https://api.trustwalletapp.com/", "https://etherscan.io/tx/", 1, true)
    }
  }

  @Singleton
  @Provides
  fun providesPageViewAnalytics(analyticsManager: AnalyticsManager): PageViewAnalytics {
    return PageViewAnalytics(analyticsManager)
  }

  @Singleton
  @Provides
  fun providesExecutorScheduler() = ExecutorScheduler(SyncExecutor(1), false)

  @Singleton
  @Provides
  fun providesGamificationMapper(context: Context) = GamificationMapper(context)

  @Singleton
  @Provides
  fun providesServicesErrorMapper() = ServicesErrorCodeMapper()

  @Singleton
  @Provides
  fun provideTransactionsDatabase(context: Context): TransactionsDatabase {
    return Room.databaseBuilder(context.applicationContext,
        TransactionsDatabase::class.java,
        "transactions_database")
        .addMigrations(
            TransactionsDatabase.MIGRATION_1_2,
            TransactionsDatabase.MIGRATION_2_3,
            TransactionsDatabase.MIGRATION_3_4,
            TransactionsDatabase.MIGRATION_4_5
        )
        .build()
  }

  @Singleton
  @Provides
  fun provideTransactionsDao(transactionsDatabase: TransactionsDatabase): TransactionsDao =
      transactionsDatabase.transactionsDao()

  @Singleton
  @Provides
  fun provideTransactionsLinkIdDao(
      transactionsDatabase: TransactionsDatabase): TransactionLinkIdDao =
      transactionsDatabase.transactionLinkIdDao()

}