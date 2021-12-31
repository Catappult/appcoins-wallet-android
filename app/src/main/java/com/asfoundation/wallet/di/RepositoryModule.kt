package com.asfoundation.wallet.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import cm.aptoide.skills.api.RoomApi
import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.repository.*
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.*
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository.AdyenApi
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.adyen.AdyenSerializer
import com.appcoins.wallet.billing.carrierbilling.CarrierBillingPreferencesRepository
import com.appcoins.wallet.billing.carrierbilling.CarrierBillingRepository
import com.appcoins.wallet.billing.carrierbilling.CarrierResponseMapper
import com.appcoins.wallet.billing.carrierbilling.response.CarrierErrorResponse
import com.appcoins.wallet.billing.carrierbilling.response.CarrierErrorResponseTypeAdapter
import com.appcoins.wallet.billing.common.BillingErrorMapper
import com.appcoins.wallet.billing.skills.SkillsPaymentRepository
import com.appcoins.wallet.commons.Logger
import com.appcoins.wallet.gamification.repository.*
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.App
import com.asfoundation.wallet.abtesting.*
import com.asfoundation.wallet.analytics.AmplitudeAnalytics
import com.asfoundation.wallet.analytics.RakamAnalytics
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.billing.address.BillingAddressRepository
import com.asfoundation.wallet.billing.partners.InstallerService
import com.asfoundation.wallet.billing.share.BdsShareLinkRepository
import com.asfoundation.wallet.billing.share.BdsShareLinkRepository.BdsShareLinkApi
import com.asfoundation.wallet.billing.share.ShareLinkRepository
import com.asfoundation.wallet.change_currency.FiatCurrenciesDao
import com.asfoundation.wallet.change_currency.FiatCurrenciesMapper
import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.eskills.withdraw.repository.SharedPreferencesWithdrawLocalStorage
import com.asfoundation.wallet.eskills.withdraw.repository.WithdrawApi
import com.asfoundation.wallet.eskills.withdraw.repository.WithdrawApiMapper
import com.asfoundation.wallet.eskills.withdraw.repository.WithdrawRepository
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepository
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.logging.send_logs.LogsDao
import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import com.asfoundation.wallet.nfts.repository.NFTRepository
import com.asfoundation.wallet.nfts.repository.NftApi
import com.asfoundation.wallet.poa.BlockchainErrorMapper
import com.asfoundation.wallet.promo_code.repository.PromoCodeDao
import com.asfoundation.wallet.promo_code.repository.PromoCodeLocalDataSource
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import com.asfoundation.wallet.rating.RatingRepository
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.repository.OffChainTransactionsRepository.TransactionsApi
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.service.AutoUpdateService
import com.asfoundation.wallet.service.GasService
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.skills.SkillsModule
import com.asfoundation.wallet.subscriptions.UserSubscriptionApi
import com.asfoundation.wallet.subscriptions.UserSubscriptionRepository
import com.asfoundation.wallet.subscriptions.UserSubscriptionsLocalData
import com.asfoundation.wallet.subscriptions.UserSubscriptionsMapper
import com.asfoundation.wallet.subscriptions.db.UserSubscriptionsDao
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.support.SupportSharedPreferences
import com.asfoundation.wallet.transactions.TransactionsMapper
import com.asfoundation.wallet.ui.backup.success.BackupSuccessLogRepository
import com.asfoundation.wallet.ui.gamification.SharedPreferencesUserStatsLocalData
import com.asfoundation.wallet.ui.iab.AppCoinsOperationMapper
import com.asfoundation.wallet.ui.iab.AppCoinsOperationRepository
import com.asfoundation.wallet.ui.iab.database.AppCoinsOperationDatabase
import com.asfoundation.wallet.ui.iab.payments.carrier.SecureCarrierBillingPreferencesRepository
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer
import com.asfoundation.wallet.verification.repository.VerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.network.BrokerVerificationApi
import com.asfoundation.wallet.wallets.db.WalletInfoDao
import com.asfoundation.wallet.wallets.repository.BalanceRepository
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule {

  @Singleton
  @Provides
  fun providePreferencesRepository(
      sharedPreferences: SharedPreferences): SharedPreferencesRepository {
    return SharedPreferencesRepository(sharedPreferences)
  }

  @Singleton
  @Provides
  fun providePreferenceRepositoryType(
      sharedPreferenceRepository: SharedPreferencesRepository): PreferencesRepositoryType {
    return sharedPreferenceRepository
  }

  @Singleton
  @Provides
  fun provideGasSettingsRepository(gasService: GasService): GasSettingsRepositoryType =
      GasSettingsRepository(gasService)

  @Singleton
  @Provides
  fun providesAppCoinsOperationRepository(@ApplicationContext context: Context): AppCoinsOperationRepository {
    return AppCoinsOperationRepository(
        Room.databaseBuilder(context.applicationContext, AppCoinsOperationDatabase::class.java,
            "appcoins_operations_data")
            .build()
            .appCoinsOperationDao(), AppCoinsOperationMapper())
  }

  @Singleton
  @Provides
  fun provideRemoteRepository(subscriptionBillingApi: SubscriptionBillingApi,
                              bdsApi: RemoteRepository.BdsApi,
                              api: BdsApiSecondary): RemoteRepository {
    return RemoteRepository(bdsApi,
        BdsApiResponseMapper(SubscriptionsMapper(), InAppMapper(ExternalBillingSerializer())), api,
        subscriptionBillingApi, ExternalBillingSerializer())
  }

  @Singleton
  @Provides
  fun provideBdsRepository(repository: RemoteRepository) = BdsRepository(repository)

  @Singleton
  @Provides
  fun provideAdyenResponseMapper(gson: Gson,
                                 billingErrorMapper: BillingErrorMapper): AdyenResponseMapper {
    return AdyenResponseMapper(gson, billingErrorMapper, AdyenSerializer())
  }

  @Singleton
  @Provides
  fun provideAdyenPaymentRepository(@Named("default") client: OkHttpClient,
                                    adyenResponseMapper: AdyenResponseMapper,
                                    bdsApi: RemoteRepository.BdsApi,
                                    subscriptionBillingApi: SubscriptionBillingApi,
                                    logger: Logger): AdyenPaymentRepository {
    val api = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_HOST + "/broker/8.20200815/gateways/adyen_v2/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(AdyenApi::class.java)
    return AdyenPaymentRepository(api, bdsApi, subscriptionBillingApi, adyenResponseMapper, logger)
  }

  @Singleton
  @Provides
  fun provideSkillsPaymentRepository(@Named("default") client: OkHttpClient,
                                     adyenResponseMapper: AdyenResponseMapper): SkillsPaymentRepository {
    val api = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_HOST + "/broker/8.20210201/gateways/adyen_v2/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(SkillsPaymentRepository.AdyenApi::class.java)
    return SkillsPaymentRepository(api, adyenResponseMapper)
  }

  @Singleton
  @Provides
  fun provideCarrierBillingRepository(@Named("default") client: OkHttpClient,
                                      preferences: CarrierBillingPreferencesRepository,
                                      billingErrorMapper: BillingErrorMapper,
                                      logger: Logger): CarrierBillingRepository {
    val gson = GsonBuilder().registerTypeAdapter(CarrierErrorResponse::class.java,
        CarrierErrorResponseTypeAdapter())
        .create()
    val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_HOST + "/broker/8.20210329/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .build()
    val api = retrofit.create(CarrierBillingRepository.CarrierBillingApi::class.java)
    return CarrierBillingRepository(api, preferences,
        CarrierResponseMapper(retrofit, billingErrorMapper), BuildConfig.APPLICATION_ID, logger)
  }

  @Singleton
  @Provides
  fun providesBillingMessageMapper(gson: Gson) = BillingErrorMapper(gson)

  @Singleton
  @Provides
  fun providesUserStatsLocalData(preferences: SharedPreferences, promotionDao: PromotionDao,
                                 levelsDao: LevelsDao, levelDao: LevelDao,
                                 walletOriginDao: WalletOriginDao): UserStatsLocalData {
    return SharedPreferencesUserStatsLocalData(preferences, promotionDao, levelsDao, levelDao,
        walletOriginDao)
  }

  @Provides
  fun providePromotionsRepository(api: GamificationApi,
                                  userStatsLocalData: UserStatsLocalData): PromotionsRepository {
    return BdsPromotionsRepository(api, userStatsLocalData)
  }

  @Singleton
  @Provides
  fun providesShareLinkRepository(api: BdsShareLinkApi): ShareLinkRepository {
    return BdsShareLinkRepository(api)
  }

  @Provides
  fun providesOffChainTransactionsRepository(
      @Named("blockchain") client: OkHttpClient): OffChainTransactionsRepository {
    val objectMapper = ObjectMapper()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    objectMapper.dateFormat = dateFormat
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val retrofit = Retrofit.Builder()
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .client(client)
        .baseUrl(BuildConfig.BACKEND_HOST)
        .build()
    return OffChainTransactionsRepository(retrofit.create(TransactionsApi::class.java),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US))
  }

  @Singleton
  @Provides
  fun provideTransactionRepository(networkInfo: NetworkInfo,
                                   accountKeystoreService: AccountKeystoreService,
                                   defaultTokenProvider: DefaultTokenProvider,
                                   nonceObtainer: MultiWalletNonceObtainer,
                                   transactionsNetworkRepository: OffChainTransactions,
                                   sharedPreferences: SharedPreferences,
                                   transactionsDao: TransactionsDao,
                                   transactionLinkIdDao: TransactionLinkIdDao): TransactionRepositoryType {
    val localRepository: TransactionsRepository =
        TransactionsLocalRepository(transactionsDao, sharedPreferences, transactionLinkIdDao)
    return BackendTransactionRepository(networkInfo, accountKeystoreService, defaultTokenProvider,
        BlockchainErrorMapper(), nonceObtainer, Schedulers.io(), transactionsNetworkRepository,
        localRepository, TransactionMapper(), TransactionsMapper(), CompositeDisposable(),
        Schedulers.io())
  }

  @Provides
  fun provideAutoUpdateRepository(autoUpdateService: AutoUpdateService) =
      AutoUpdateRepository(autoUpdateService)

  @Singleton
  @Provides
  fun provideIdsRepository(@ApplicationContext context: Context,
                           sharedPreferencesRepository: SharedPreferencesRepository,
                           userStatsLocalData: UserStatsLocalData,
                           installerService: InstallerService): IdsRepository {
    return IdsRepository(context.contentResolver, sharedPreferencesRepository, userStatsLocalData,
        installerService)
  }

  @Singleton
  @Provides
  fun provideWalletRepository(preferencesRepositoryType: PreferencesRepositoryType,
                              accountKeystoreService: AccountKeystoreService,
                              analyticsSetup: RakamAnalytics,
                              amplitudeAnalytics: AmplitudeAnalytics): WalletRepositoryType {
    return WalletRepository(preferencesRepositoryType, accountKeystoreService, Schedulers.io(),
        analyticsSetup, amplitudeAnalytics)
  }

  @Singleton
  @Provides
  fun provideTokenRepository(): TokenRepository {
    return TokenRepository()
  }

  @Singleton
  @Provides
  fun provideSupportRepository(preferences: SupportSharedPreferences, app: Application): SupportRepository {
    return SupportRepository(preferences, app as App)
  }

  @Singleton
  @Provides
  fun provideFingerprintPreferenceRepository(
      preferences: SharedPreferences): FingerprintPreferencesRepositoryContract {
    return FingerprintPreferencesRepository(preferences)
  }

  @Singleton
  @Provides
  fun providesBackupRestorePreferencesRepository(
      sharedPreferences: SharedPreferences): BackupRestorePreferencesRepository {
    return BackupRestorePreferencesRepository(sharedPreferences)
  }

  @Singleton
  @Provides
  fun providesABTestRepository(abTestApi: ABTestApi, idsRepository: IdsRepository,
                               @Named("ab-test-local-cache")
                               localCache: HashMap<String, ExperimentModel>,
                               persistence: RoomExperimentPersistence,
                               cacheValidator: ABTestCacheValidator): ABTestRepository {
    return ABTestCenterRepository(abTestApi, idsRepository, localCache, persistence, cacheValidator,
        Schedulers.io())
  }

  @Singleton
  @Provides
  fun providesGasPreferenceRepository(
      sharedPreferences: SharedPreferences): GasPreferenceRepository {
    return GasPreferenceRepository(sharedPreferences)
  }

  @Singleton
  @Provides
  fun providesBillingAddressRepository(
      secureSharedPreferences: SecureSharedPreferences): BillingAddressRepository {
    return BillingAddressRepository(secureSharedPreferences)
  }

  @Singleton
  @Provides
  fun providesRatingRepository(sharedPreferences: SharedPreferences,
                               walletFeedbackApi: RatingRepository.WalletFeedbackApi,
                               logger: Logger): RatingRepository {
    return RatingRepository(sharedPreferences, walletFeedbackApi, logger)
  }

  @Singleton
  @Provides
  fun providesCarrierBillingPreferencesRepository(
      secureSharedPreferences: SecureSharedPreferences): CarrierBillingPreferencesRepository {
    return SecureCarrierBillingPreferencesRepository(secureSharedPreferences)
  }

  @Singleton
  @Provides
  fun provideWalletVerificationRepository(walletInfoRepository: WalletInfoRepository,
                                          brokerVerificationApi: BrokerVerificationApi,
                                          adyenResponseMapper: AdyenResponseMapper,
                                          sharedPreferences: SharedPreferences): VerificationRepository {
    return VerificationRepository(walletInfoRepository, brokerVerificationApi, adyenResponseMapper,
        sharedPreferences)
  }

  @Singleton
  @Provides
  fun providesUserSubscriptionsLocalData(
      userSubscriptionsDao: UserSubscriptionsDao): UserSubscriptionsLocalData {
    return UserSubscriptionsLocalData(userSubscriptionsDao)
  }

  @Singleton
  @Provides
  fun provideSubscriptionRepository(userSubscriptionApi: UserSubscriptionApi,
                                    userSubscriptionsLocalData: UserSubscriptionsLocalData,
                                    walletService: WalletService): UserSubscriptionRepository {
    return UserSubscriptionRepository(userSubscriptionApi, userSubscriptionsLocalData,
        walletService, UserSubscriptionsMapper())
  }

  @Singleton
  @Provides
  fun providesWithdrawRepository(api: WithdrawApi, gson: Gson, sharedPreferences: SharedPreferences,
                                 schedulers: RxSchedulers): WithdrawRepository {
    return WithdrawRepository(api, WithdrawApiMapper(gson), schedulers,
        SharedPreferencesWithdrawLocalStorage(sharedPreferences))
  }

  @Singleton
  @Provides
  fun providesFiatCurrenciesRepository(@Named("default") client: OkHttpClient,
                                       objectMapper: ObjectMapper,
                                       sharedPreferences: SharedPreferences,
                                       fiatCurrenciesDao: FiatCurrenciesDao,
                                       conversionService: LocalCurrencyConversionService): FiatCurrenciesRepository {
    val baseUrl = BuildConfig.BASE_HOST
    val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(FiatCurrenciesRepository.FiatCurrenciesApi::class.java)
    return FiatCurrenciesRepository(api, sharedPreferences, FiatCurrenciesMapper(),
        fiatCurrenciesDao, conversionService)

  }

  @Singleton
  @Provides
  fun providesPromoCodeRepository(@Named("default") client: OkHttpClient,
                                  promoCodeLocalDataSource: PromoCodeLocalDataSource,
                                  analyticsSetup: RakamAnalytics,
                                  rxSchedulers: RxSchedulers): PromoCodeRepository {
    val msBaseUrl = BuildConfig.BASE_HOST
    val backendBaseUrl = BuildConfig.BACKEND_HOST
    val msApi = Retrofit.Builder()
        .baseUrl(msBaseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(PromoCodeRepository.PromoCodeApi::class.java)
    val backendApi = Retrofit.Builder()
        .baseUrl(backendBaseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(PromoCodeRepository.PromoCodeBackendApi::class.java)
    return PromoCodeRepository(msApi, backendApi, promoCodeLocalDataSource, analyticsSetup,
        rxSchedulers)
  }

  @Singleton
  @Provides
  fun providesPromoCodeLocalDataSource(promoCodeDao: PromoCodeDao,
                                       rxSchedulers: RxSchedulers): PromoCodeLocalDataSource {
    return PromoCodeLocalDataSource(promoCodeDao, rxSchedulers)
  }

  @Singleton
  @Provides
  fun providesSendLogsRepository(@Named("default") client: OkHttpClient, logsDao: LogsDao,
                                 rxSchedulers: RxSchedulers, @ApplicationContext context: Context): SendLogsRepository {
    val api = Retrofit.Builder()
        .baseUrl(BuildConfig.BACKEND_HOST)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(SendLogsRepository.SendLogsApi::class.java)
    val awsApi = Retrofit.Builder()
        .baseUrl("https://localhost/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(SendLogsRepository.AwsUploadFilesApi::class.java)
    return SendLogsRepository(api, awsApi, logsDao, rxSchedulers, context.cacheDir)
  }

  @Singleton
  @Provides
  fun providesNFTRepository(@Named("default") client: OkHttpClient,
                            rxSchedulers: RxSchedulers): NFTRepository {
    val baseUrl = BuildConfig.BACKEND_HOST
    val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(NftApi::class.java)
    return NFTRepository(api, rxSchedulers)
  }

  @Singleton
  @Provides
  fun providesWalletInfoRepository(@Named("default") client: OkHttpClient,
                                   walletInfoDao: WalletInfoDao,
                                   balanceRepository: BalanceRepository,
                                   rxSchedulers: RxSchedulers): WalletInfoRepository {
    val api = Retrofit.Builder()
        .baseUrl(BuildConfig.BACKEND_HOST)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(WalletInfoRepository.WalletInfoApi::class.java)
    return WalletInfoRepository(api, walletInfoDao, balanceRepository, rxSchedulers)
  }

  @Singleton
  @Provides
  fun providesBackupSuccessLogRepository(@Named("default") client: OkHttpClient,
                                         rxSchedulers: RxSchedulers): BackupSuccessLogRepository {
    val baseUrl = BuildConfig.BACKEND_HOST
    val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(BackupSuccessLogRepository.BackupLopApi::class.java)
    return BackupSuccessLogRepository(api, rxSchedulers)

  }

  @Singleton
  @Provides
  fun providesBalanceRepository(getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase,
                                localCurrencyConversionService: LocalCurrencyConversionService,
                                rxSchedulers: RxSchedulers): BalanceRepository {
    return BalanceRepository(getSelectedCurrencyUseCase, localCurrencyConversionService,
        rxSchedulers)
  }

  @Provides
  fun providesLoginRepository(roomApi: RoomApi): LoginRepository {
    return LoginRepository(roomApi)
  }

  @Provides
  fun providesRoomRepository(roomApi: RoomApi): RoomRepository {
    return RoomRepository(roomApi)
  }

  @Provides
  fun providesRoomApi(@Named("default") client: OkHttpClient): RoomApi {
    val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm")
        .create()

    return Retrofit.Builder()
        .baseUrl(SkillsModule.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(RoomApi::class.java)
  }

  @Provides
  fun providesTicketsRepository(@Named("default") client: OkHttpClient,
                                sharedPreferences: SharedPreferences): TicketRepository {
    val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm")
        .create()

    val api = Retrofit.Builder()
        .baseUrl(SkillsModule.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(TicketApi::class.java)

    return TicketRepository(api, SharedPreferencesTicketLocalStorage(sharedPreferences, gson),
        TicketApiMapper(gson))
  }
}