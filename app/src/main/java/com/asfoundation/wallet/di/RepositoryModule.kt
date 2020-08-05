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
import com.appcoins.wallet.gamification.repository.BdsPromotionsRepository
import com.appcoins.wallet.gamification.repository.GamificationApi
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.analytics.RakamAnalytics
import com.asfoundation.wallet.billing.partners.InstallerService
import com.asfoundation.wallet.billing.purchase.InAppDeepLinkRepository
import com.asfoundation.wallet.billing.purchase.LocalPayementsLinkRepository
import com.asfoundation.wallet.billing.purchase.LocalPayementsLinkRepository.DeepLinkApi
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
import com.asfoundation.wallet.repository.TransactionsDatabase.Companion.MIGRATION_1_2
import com.asfoundation.wallet.repository.TransactionsDatabase.Companion.MIGRATION_2_3
import com.asfoundation.wallet.service.*
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
  fun provideAdyenPaymentRepository(client: OkHttpClient): AdyenPaymentRepository {
    val api = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_HOST + "/broker/8.20200801/gateways/adyen_v2/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(AdyenApi::class.java)
    return AdyenPaymentRepository(api, AdyenResponseMapper())
  }

  @Provides
  fun providePromotionsRepository(api: GamificationApi,
                                  preferences: SharedPreferences): PromotionsRepository {
    return BdsPromotionsRepository(api, SharedPreferencesGamificationLocalData(preferences),
        getVersionCode())
  }

  @Singleton
  @Provides
  fun providesShareLinkRepository(api: BdsShareLinkApi): ShareLinkRepository {
    return BdsShareLinkRepository(api)
  }

  @Provides
  fun providesOffChainTransactionsRepository(client: OkHttpClient): OffChainTransactionsRepository {
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
                                   context: Context,
                                   sharedPreferences: SharedPreferences): TransactionRepositoryType {

    val transactionsDao = Room.databaseBuilder(context.applicationContext,
        TransactionsDatabase::class.java,
        "transactions_database")
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()
        .transactionsDao()
    val localRepository: TransactionsRepository =
        TransactionsLocalRepository(transactionsDao, sharedPreferences)
    return BackendTransactionRepository(networkInfo, accountKeystoreService,
        defaultTokenProvider, BlockchainErrorMapper(), nonceObtainer,
        Schedulers.io(),
        transactionsNetworkRepository, localRepository, TransactionMapper(),
        CompositeDisposable(), Schedulers.io())
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

  private fun getVersionCode() = BuildConfig.VERSION_CODE.toString()

  @Singleton
  @Provides
  fun provideWalletRepository(preferencesRepositoryType: PreferencesRepositoryType,
                              accountKeystoreService: AccountKeystoreService,
                              walletBalanceService: WalletBalanceService,
                              analyticsSetup: RakamAnalytics): WalletRepositoryType {
    return WalletRepository(preferencesRepositoryType, accountKeystoreService,
        walletBalanceService, Schedulers.io(), analyticsSetup)
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
    return LocalPayementsLinkRepository(api)
  }

}