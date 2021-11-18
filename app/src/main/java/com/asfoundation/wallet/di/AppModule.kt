package com.asfoundation.wallet.di

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
import androidx.biometric.BiometricManager
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.adyen.checkout.core.api.Environment
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards
import com.appcoins.wallet.appcoins.rewards.ErrorMapper
import com.appcoins.wallet.appcoins.rewards.repository.BdsAppcoinsRewardsRepository
import com.appcoins.wallet.appcoins.rewards.repository.backend.BackendApi
import com.appcoins.wallet.bdsbilling.*
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.commons.LogReceiver
import com.appcoins.wallet.commons.Logger
import com.appcoins.wallet.commons.MemoryCache
import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.PromotionDatabase
import com.appcoins.wallet.gamification.repository.PromotionDatabase.Companion.MIGRATION_1_2
import com.appcoins.wallet.gamification.repository.PromotionDatabase.Companion.MIGRATION_2_3
import com.appcoins.wallet.gamification.repository.PromotionDatabase.Companion.MIGRATION_3_4
import com.appcoins.wallet.gamification.repository.PromotionDatabase.Companion.MIGRATION_4_5
import com.appcoins.wallet.gamification.repository.PromotionDatabase.Companion.MIGRATION_5_6
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.WalletOriginDao
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
import com.asfoundation.wallet.abtesting.*
import com.asfoundation.wallet.abtesting.experiments.topup.TopUpDefaultValueExperiment
import com.asfoundation.wallet.analytics.TaskTimer
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.base.RxSchedulersImpl
import com.asfoundation.wallet.billing.CreditsRemoteRepository
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.change_currency.FiatCurrenciesDao
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.ewt.EwtAuthenticatorService
import com.asfoundation.wallet.interact.BalanceGetter
import com.asfoundation.wallet.interact.BuildConfigDefaultTokenProvider
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.logging.DebugReceiver
import com.asfoundation.wallet.logging.WalletLogger
import com.asfoundation.wallet.logging.send_logs.LogsDao
import com.asfoundation.wallet.logging.send_logs.LogsDatabase
import com.asfoundation.wallet.logging.send_logs.SendLogsReceiver
import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import com.asfoundation.wallet.permissions.repository.PermissionRepository
import com.asfoundation.wallet.permissions.repository.PermissionsDatabase
import com.asfoundation.wallet.poa.*
import com.asfoundation.wallet.promo_code.repository.PromoCodeDao
import com.asfoundation.wallet.promo_code.repository.PromoCodeDatabase
import com.asfoundation.wallet.promotions.model.PromotionsMapper
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.repository.IpCountryCodeProvider.IpApi
import com.asfoundation.wallet.router.GasSettingsRouter
import com.asfoundation.wallet.service.CampaignService
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.asfoundation.wallet.service.TokenRateService
import com.asfoundation.wallet.service.currencies.CurrenciesDatabase
import com.asfoundation.wallet.service.currencies.CurrencyConversionRatesPersistence
import com.asfoundation.wallet.service.currencies.RoomCurrencyConversionRatesPersistence
import com.asfoundation.wallet.subscriptions.db.UserSubscriptionsDao
import com.asfoundation.wallet.subscriptions.db.UserSubscriptionsDatabase
import com.asfoundation.wallet.support.SupportSharedPreferences
import com.asfoundation.wallet.topup.TopUpValuesApiResponseMapper
import com.asfoundation.wallet.transactions.TransactionsMapper
import com.asfoundation.wallet.ui.airdrop.AirdropChainIdMapper
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.iab.*
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer
import com.asfoundation.wallet.ui.iab.raiden.NonceObtainerFactory
import com.asfoundation.wallet.ui.iab.raiden.Web3jNonceProvider
import com.asfoundation.wallet.util.*
import com.asfoundation.wallet.util.CurrencyFormatUtils.Companion.create
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoProvider
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.JsonObject
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
internal class AppModule {
  @Provides
  fun provideContext(application: App): Context = application.applicationContext

  @Provides
  @Singleton
  fun provideRxSchedulers(): RxSchedulers {
    return RxSchedulersImpl()
  }

  @Singleton
  @Provides
  fun provideGson() = Gson()

  @Singleton
  @Provides
  fun provideLogInterceptor(logsDao: LogsDao) = LogInterceptor(logsDao)

  @Singleton
  @Provides
  @Named("blockchain")
  fun provideBlockchainOkHttpClient(context: Context,
                                    preferencesRepositoryType: PreferencesRepositoryType,
                                    logInterceptor: LogInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor(context, preferencesRepositoryType))
        .addInterceptor(logInterceptor)
        .connectTimeout(15, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.MINUTES)
        .writeTimeout(30, TimeUnit.MINUTES)
        .build()
  }

  @Singleton
  @Provides
  @Named("default")
  fun provideDefaultOkHttpClient(context: Context,
                                 preferencesRepositoryType: PreferencesRepositoryType,
                                 logInterceptor: LogInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor(context, preferencesRepositoryType))
        .addInterceptor(logInterceptor)
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
  }

  @Singleton
  @Provides
  @Named("low-timer")
  fun provideLowTimerOkHttpClient(context: Context,
                                  preferencesRepositoryType: PreferencesRepositoryType,
                                  logInterceptor: LogInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor(context, preferencesRepositoryType))
        .addInterceptor(logInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()
  }

  @Singleton
  @Provides
  fun passwordStore(context: Context, logger: Logger): PasswordStore {
    return TrustPasswordStore(context, logger)
  }

  @Singleton
  @Provides
  fun provideLogger(sendLogsRepository: SendLogsRepository): Logger {
    val receivers = ArrayList<LogReceiver>()
    if (BuildConfig.DEBUG) {
      receivers.add(DebugReceiver())
    }
    receivers.add(SendLogsReceiver(sendLogsRepository))
    return WalletLogger(receivers)
  }

  @Singleton
  @Provides
  fun providesBillingPaymentProofSubmission(api: RemoteRepository.BdsApi,
                                            walletService: WalletService,
                                            subscriptionBillingApi: SubscriptionBillingApi,
                                            bdsApi: BdsApiSecondary,
                                            billingSerializer: ExternalBillingSerializer): BillingPaymentProofSubmission {
    return BillingPaymentProofSubmissionImpl.Builder()
        .setApi(api)
        .setBillingSerializer(billingSerializer)
        .setBdsApiSecondary(bdsApi)
        .setWalletService(walletService)
        .setSubscriptionBillingService(subscriptionBillingApi)
        .build()
  }

  @Singleton
  @Provides
  fun providePaymentErrorMapper(gson: Gson) = PaymentErrorMapper(gson)

  @Provides
  fun provideGasSettingsRouter() = GasSettingsRouter()

  @Provides
  fun providePaymentMethodsMapper(
      billingMessagesMapper: BillingMessagesMapper): PaymentMethodsMapper {
    return PaymentMethodsMapper(billingMessagesMapper)
  }

  @Provides
  fun provideNonceObtainer(web3jProvider: Web3jProvider): MultiWalletNonceObtainer {
    return MultiWalletNonceObtainer(NonceObtainerFactory(30000, Web3jNonceProvider(web3jProvider)))
  }

  @Provides
  fun provideEIPTransferParser(defaultTokenProvider: DefaultTokenProvider): EIPTransactionParser {
    return EIPTransactionParser(defaultTokenProvider)
  }

  @Provides
  fun provideOneStepTransferParser(proxyService: ProxyService, billing: Billing,
                                   tokenRateService: TokenRateService,
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
        AppInfoProvider(context, ImageSaver(context.filesDir.toString() + "/app_icons/")),
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
                             remoteRepository: RemoteRepository,
                             errorMapper: ErrorMapper): AppcoinsRewards {
    return AppcoinsRewards(
        BdsAppcoinsRewardsRepository(CreditsRemoteRepository(backendApi, remoteRepository)),
        object : com.appcoins.wallet.appcoins.rewards.repository.WalletService {
          override fun getWalletAddress() = walletService.getWalletAddress()

          override fun signContent(content: String) = walletService.signContent(content)
        }, MemoryCache(BehaviorSubject.create(), ConcurrentHashMap()), Schedulers.io(), billing,
        errorMapper)
  }

  @Singleton
  @Provides
  fun provideRewardsManager(appcoinsRewards: AppcoinsRewards, billing: Billing,
                            addressService: AddressService): RewardsManager {
    return RewardsManager(appcoinsRewards, billing, addressService)
  }

  @Singleton
  @Provides
  fun provideBillingMessagesMapper(billingSerializer: ExternalBillingSerializer) =
      BillingMessagesMapper(billingSerializer)

  @Singleton
  @Provides
  fun provideBillingSerializer() = ExternalBillingSerializer()

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
  fun providesPermissions(context: Context): Permissions {
    return Permissions(PermissionRepository(
        Room.databaseBuilder(context.applicationContext, PermissionsDatabase::class.java,
            "permissions_database")
            .build()
            .permissionsDao()))
  }

  @Singleton
  @Provides
  fun providesPromotionDatabase(context: Context): PromotionDatabase {
    return Room.databaseBuilder(context, PromotionDatabase::class.java, "promotion_database")
        .addMigrations(MIGRATION_1_2)
        .addMigrations(MIGRATION_2_3)
        .addMigrations(MIGRATION_3_4)
        .addMigrations(MIGRATION_4_5)
        .addMigrations(MIGRATION_5_6)
        .build()
  }

  @Singleton
  @Provides
  fun providesPromotionDao(promotionDatabase: PromotionDatabase) = promotionDatabase.promotionDao()

  @Singleton
  @Provides
  fun providesLevelsDao(promotionDatabase: PromotionDatabase) = promotionDatabase.levelsDao()

  @Singleton
  @Provides
  fun providesLevelDao(promotionDatabase: PromotionDatabase) = promotionDatabase.levelDao()

  @Singleton
  @Provides
  fun providesWalletOriginDao(promotionDatabase: PromotionDatabase): WalletOriginDao {
    return promotionDatabase.walletOriginDao()
  }

  @Singleton
  @Provides
  fun providesUserSubscriptionsDatabase(context: Context): UserSubscriptionsDatabase {
    return Room.databaseBuilder(context, UserSubscriptionsDatabase::class.java,
        "user_subscription_database")
        .build()
  }

  @Singleton
  @Provides
  fun providesUserSubscriptionDao(
      userSubscriptionsDatabase: UserSubscriptionsDatabase): UserSubscriptionsDao {
    return userSubscriptionsDatabase.subscriptionsDao()
  }

  @Provides
  fun providesObjectMapper(): ObjectMapper {
    return ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  }

  @Provides
  fun providesTopUpValuesApiResponseMapper() = TopUpValuesApiResponseMapper()

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
      val notificationChannel = NotificationChannel(channelId, channelName, importance)
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

  @Provides
  @Named("local_version_code")
  fun provideLocalVersionCode(context: Context, packageManager: PackageManager): Int {
    return try {
      packageManager.getPackageInfo(context.packageName, 0).versionCode
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
  fun provideCurrencyFormatUtils() = create()

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
  fun providesExecutorScheduler() = ExecutorScheduler(SyncExecutor(1), false)

  @Singleton
  @Provides
  fun providesGamificationMapper(context: Context) = GamificationMapper(context)

  @Singleton
  @Provides
  fun providesPromotionsMapper(gamificationMapper: GamificationMapper) =
      PromotionsMapper(gamificationMapper)

  @Singleton
  @Provides
  fun providesServicesErrorMapper() = ServicesErrorCodeMapper()

  @Singleton
  @Provides
  fun providesBiometricManager(context: Context) = BiometricManager.from(context)

  @Singleton
  @Provides
  fun provideTransactionsDatabase(context: Context): TransactionsDatabase {
    return Room.databaseBuilder(context.applicationContext, TransactionsDatabase::class.java,
        "transactions_database")
        .addMigrations(TransactionsDatabase.MIGRATION_1_2, TransactionsDatabase.MIGRATION_2_3,
            TransactionsDatabase.MIGRATION_3_4, TransactionsDatabase.MIGRATION_4_5,
            TransactionsDatabase.MIGRATION_5_6, TransactionsDatabase.MIGRATION_6_7)
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


  @Singleton
  @Provides
  fun providesApplicationInfoLoader(context: Context) = ApplicationInfoProvider(context)

  @Singleton
  @Provides
  fun providesStringProvider(context: Context): StringProvider = StringProvider(context.resources)

  @Singleton
  @Provides
  @Named("ab-test-local-cache")
  fun providesAbTestLocalCache(): HashMap<String, ExperimentModel> {
    return HashMap()
  }

  @Singleton
  @Provides
  fun providesAbTestCacheValidator(@Named("ab-test-local-cache")
                                   localCache: HashMap<String, ExperimentModel>): ABTestCacheValidator {
    return ABTestCacheValidator(localCache)
  }

  @Singleton
  @Provides
  fun providesAbTestDatabase(context: Context): ABTestDatabase {
    return Room.databaseBuilder(context, ABTestDatabase::class.java, "abtest_database")
        .build()
  }

  @Singleton
  @Provides
  fun providesRoomExperimentPersistence(abTestDatabase: ABTestDatabase): RoomExperimentPersistence {
    return RoomExperimentPersistence(abTestDatabase.experimentDao(), RoomExperimentMapper(),
        Schedulers.io())
  }

  @Singleton
  @Provides
  fun providesTopUpDefaultValueExperiment(
      abTestInteractor: ABTestInteractor): TopUpDefaultValueExperiment {
    return TopUpDefaultValueExperiment(abTestInteractor)
  }

  @Singleton
  @Provides
  fun providesSecureSharedPreferences(context: Context): SecureSharedPreferences {
    return SecureSharedPreferences(context)
  }

  @Singleton
  @Provides
  fun providesErrorMapper(gson: Gson): ErrorMapper {
    return ErrorMapper(gson)
  }


  @Singleton
  @Provides
  fun provideCurrencyConversionRatesDatabase(context: Context): CurrenciesDatabase {
    return Room.databaseBuilder(context, CurrenciesDatabase::class.java, "currencies_database")
        .addMigrations(
            CurrenciesDatabase.MIGRATION_1_2,
        )
        .build()
  }

  @Singleton
  @Provides
  fun provideRoomCurrencyConversionRatesPersistence(
      database: CurrenciesDatabase): CurrencyConversionRatesPersistence {
    return RoomCurrencyConversionRatesPersistence(database.currencyConversionRatesDao())
  }

  @Singleton
  @Provides
  fun provideFiatCurrenciesDao(database: CurrenciesDatabase): FiatCurrenciesDao {
    return database.fiatCurrenciesDao()
  }

  @Singleton
  @Provides
  fun provideLogsDatabase(context: Context): LogsDatabase {
    return Room.databaseBuilder(context, LogsDatabase::class.java, "logs_database")
        .build()
  }

  @Singleton
  @Provides
  fun provideLogsDao(database: LogsDatabase): LogsDao {
    return database.logsDao()
  }


  @Singleton
  @Provides
  fun provideTaskTimer(): TaskTimer {
    return TaskTimer()
  }

  @Provides
  fun providesEwtAuthService(walletService: WalletService): EwtAuthenticatorService {
    val headerJson = JsonObject()
    headerJson.addProperty("typ", "EWT")
    return EwtAuthenticatorService(walletService, headerJson.toString())
  }

  @Singleton
  @Provides
  fun providePromoCodeDatabase(context: Context): PromoCodeDatabase {
    return Room.databaseBuilder(context, PromoCodeDatabase::class.java, "promo_code_database")
        .addMigrations(PromoCodeDatabase.MIGRATION_1_2)
        .build()
  }

  @Singleton
  @Provides
  fun providePromoCodeDao(database: PromoCodeDatabase): PromoCodeDao {
    return database.promoCodeDao()
  }
}