package com.asfoundation.wallet.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.appcoins.wallet.bdsbilling.repository.BdsApiResponseMapper
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository.BdsApi
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository.AdyenApi
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper
import com.appcoins.wallet.billing.carrierbilling.CarrierBillingRepository
import com.appcoins.wallet.billing.carrierbilling.CarrierResponseMapper
import com.appcoins.wallet.billing.carrierbilling.response.CarrierErrorResponse
import com.appcoins.wallet.billing.carrierbilling.response.CarrierErrorResponseTypeAdapter
import com.appcoins.wallet.gamification.repository.*
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.App
import com.asfoundation.wallet.analytics.AmplitudeAnalytics
import com.asfoundation.wallet.analytics.RakamAnalytics
import com.asfoundation.wallet.billing.partners.InstallerService
import com.asfoundation.wallet.billing.purchase.InAppDeepLinkRepository
import com.asfoundation.wallet.billing.purchase.LocalPaymentsLinkRepository
import com.asfoundation.wallet.billing.purchase.LocalPaymentsLinkRepository.DeepLinkApi
import com.asfoundation.wallet.billing.share.BdsShareLinkRepository
import com.asfoundation.wallet.billing.share.BdsShareLinkRepository.BdsShareLinkApi
import com.asfoundation.wallet.billing.share.ShareLinkRepository
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.interact.GetDefaultWalletBalanceInteract
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.poa.BlockchainErrorMapper
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.repository.OffChainTransactionsRepository.TransactionsApi
import com.asfoundation.wallet.service.*
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.support.SupportSharedPreferences
import com.asfoundation.wallet.transactions.TransactionsMapper
import com.asfoundation.wallet.ui.balance.AppcoinsBalanceRepository
import com.asfoundation.wallet.ui.balance.BalanceRepository
import com.asfoundation.wallet.ui.balance.database.BalanceDetailsDatabase
import com.asfoundation.wallet.ui.balance.database.BalanceDetailsMapper
import com.asfoundation.wallet.ui.gamification.SharedPreferencesGamificationLocalData
import com.asfoundation.wallet.ui.iab.AppCoinsOperationMapper
import com.asfoundation.wallet.ui.iab.AppCoinsOperationRepository
import com.asfoundation.wallet.ui.iab.database.AppCoinsOperationDatabase
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer
import com.asfoundation.wallet.wallet_blocked.WalletStatusApi
import com.asfoundation.wallet.wallet_blocked.WalletStatusRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

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
  fun providesAppCoinsOperationRepository(context: Context): AppCoinsOperationRepository {
    return AppCoinsOperationRepository(
        Room.databaseBuilder(context.applicationContext, AppCoinsOperationDatabase::class.java,
            "appcoins_operations_data")
            .build()
            .appCoinsOperationDao(), AppCoinsOperationMapper())
  }

  @Singleton
  @Provides
  fun provideRemoteRepository(bdsApi: BdsApi, api: BdsApiSecondary): RemoteRepository {
    return RemoteRepository(bdsApi, BdsApiResponseMapper(), api)
  }

  @Singleton
  @Provides
  fun provideBdsRepository(repository: RemoteRepository) = BdsRepository(repository)

  @Singleton
  @Provides
  fun provideAdyenPaymentRepository(
      @Named("default") client: OkHttpClient): AdyenPaymentRepository {
    val api = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_HOST + "/broker/8.20200815/gateways/adyen_v2/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(AdyenApi::class.java)
    return AdyenPaymentRepository(api, AdyenResponseMapper())
  }

  @Singleton
  @Provides
  fun provideCarrierBillingRepository(
      @Named("default") client: OkHttpClient): CarrierBillingRepository {
    val gson = GsonBuilder().registerTypeAdapter(CarrierErrorResponse::class.java,
        CarrierErrorResponseTypeAdapter())
        .create()
    val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_HOST + "/broker/8.20201101/gateways/dimoco/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .build()
    val api = retrofit.create(CarrierBillingRepository.CarrierBillingApi::class.java)
    return CarrierBillingRepository(api, CarrierResponseMapper(retrofit),
        BuildConfig.APPLICATION_ID)
  }

  @Provides
  fun providePromotionsRepository(api: GamificationApi, preferences: SharedPreferences,
                                  promotionDao: PromotionDao, levelsDao: LevelsDao,
                                  levelDao: LevelDao): PromotionsRepository {
    return BdsPromotionsRepository(api,
        SharedPreferencesGamificationLocalData(preferences, promotionDao, levelsDao, levelDao))
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
    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    objectMapper.dateFormat = df
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

  @Singleton
  @Provides
  fun provideBalanceRepository(context: Context,
                               localCurrencyConversionService: LocalCurrencyConversionService,
                               getDefaultWalletBalanceInteract: GetDefaultWalletBalanceInteract): BalanceRepository {
    return AppcoinsBalanceRepository(getDefaultWalletBalanceInteract,
        localCurrencyConversionService,
        Room.databaseBuilder(context.applicationContext,
            BalanceDetailsDatabase::class.java,
            "balance_details")
            .build()
            .balanceDetailsDao(), BalanceDetailsMapper(), Schedulers.io())
  }

  @Provides
  fun provideAutoUpdateRepository(autoUpdateService: AutoUpdateService) =
      AutoUpdateRepository(autoUpdateService)

  @Singleton
  @Provides
  fun provideIdsRepository(context: Context,
                           sharedPreferencesRepository: SharedPreferencesRepository,
                           installerService: InstallerService): IdsRepository {
    return IdsRepository(context.contentResolver, sharedPreferencesRepository, installerService)
  }

  @Singleton
  @Provides
  fun provideWalletRepository(preferencesRepositoryType: PreferencesRepositoryType,
                              accountKeystoreService: AccountKeystoreService,
                              walletBalanceService: WalletBalanceService,
                              analyticsSetup: RakamAnalytics,
                              amplitudeAnalytics: AmplitudeAnalytics): WalletRepositoryType {
    return WalletRepository(preferencesRepositoryType, accountKeystoreService,
        walletBalanceService, Schedulers.io(), analyticsSetup, amplitudeAnalytics)
  }

  @Singleton
  @Provides
  fun provideTokenRepository(
      defaultTokenProvider: DefaultTokenProvider,
      walletRepositoryType: WalletRepositoryType): TokenRepositoryType {
    return TokenRepository(defaultTokenProvider, walletRepositoryType)
  }

  @Singleton
  @Provides
  fun provideSmsValidationRepository(
      smsValidationApi: SmsValidationApi, gson: Gson,
      logger: Logger): SmsValidationRepositoryType {
    return SmsValidationRepository(smsValidationApi, gson, logger)
  }

  @Singleton
  @Provides
  fun provideWalletStatusRepository(
      walletStatusApi: WalletStatusApi): WalletStatusRepository {
    return WalletStatusRepository(walletStatusApi)
  }

  @Singleton
  @Provides
  fun providesDeepLinkRepository(api: DeepLinkApi): InAppDeepLinkRepository {
    return LocalPaymentsLinkRepository(api)
  }

  @Singleton
  @Provides
  fun provideSupportRepository(preferences: SupportSharedPreferences, app: App): SupportRepository {
    return SupportRepository(preferences, app)
  }
}