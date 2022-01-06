package com.asfoundation.wallet.di.api

import com.appcoins.wallet.gamification.repository.GamificationApi
import com.appcoins.wallet.gamification.repository.entity.PromotionsDeserializer
import com.appcoins.wallet.gamification.repository.entity.PromotionsResponse
import com.appcoins.wallet.gamification.repository.entity.PromotionsSerializer
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.di.annotations.*
import com.asfoundation.wallet.eskills.withdraw.repository.WithdrawApi
import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import com.asfoundation.wallet.nfts.repository.NftApi
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import com.asfoundation.wallet.repository.IpCountryCodeProvider
import com.asfoundation.wallet.repository.OffChainTransactionsRepository
import com.asfoundation.wallet.service.AutoUpdateService
import com.asfoundation.wallet.service.CampaignService
import com.asfoundation.wallet.service.GasService
import com.asfoundation.wallet.service.TokenRateService
import com.asfoundation.wallet.ui.backup.success.BackupSuccessLogRepository
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BackendApiModule {

  private val backendUrl = BuildConfig.BACKEND_HOST

  @Singleton
  @Provides
  @BackendBlockchainRetrofit
  fun provideBackendBlockchainRetrofit(@BlockchainHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(backendUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  @BackendDefaultRetrofit
  fun provideBackendDefaultRetrofit(@DefaultHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(backendUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  @BackendLowTimerRetrofit
  fun provideBackendLowTimerRetrofit(@LowTimerHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(backendUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  fun providesIpCountryCodeApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): IpCountryCodeProvider.IpApi {
    return retrofit.create(IpCountryCodeProvider.IpApi::class.java)
  }

  @Singleton
  @Provides
  fun providesOffChainTransactionsApi(
    @BackendBlockchainRetrofit retrofit: Retrofit
  ): OffChainTransactionsRepository.TransactionsApi {
    return retrofit.create(OffChainTransactionsRepository.TransactionsApi::class.java)
  }

  @Singleton
  @Provides
  fun providesPromoCodeBackendApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): PromoCodeRepository.PromoCodeBackendApi {
    return retrofit.create(PromoCodeRepository.PromoCodeBackendApi::class.java)
  }

  @Singleton
  @Provides
  fun providesSendLogsApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): SendLogsRepository.SendLogsApi {
    return retrofit.create(SendLogsRepository.SendLogsApi::class.java)
  }

  @Singleton
  @Provides
  fun providesNftApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): NftApi {
    return retrofit.create(NftApi::class.java)
  }

  @Singleton
  @Provides
  fun providesWalletInfoApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): WalletInfoRepository.WalletInfoApi {
    return retrofit.create(WalletInfoRepository.WalletInfoApi::class.java)
  }

  @Singleton
  @Provides
  fun providesBackupSuccessLogApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): BackupSuccessLogRepository.BackupLogApi {
    return retrofit.create(BackupSuccessLogRepository.BackupLogApi::class.java)
  }

  @Singleton
  @Provides
  fun provideTokenToFiatApi(
    @BackendBlockchainRetrofit retrofit: Retrofit
  ): TokenRateService.TokenToFiatApi {
    return retrofit.create(TokenRateService.TokenToFiatApi::class.java)
  }

  @Singleton
  @Provides
  fun provideCampaignServiceApi(
    @BackendBlockchainRetrofit retrofit: Retrofit
  ): CampaignService.CampaignApi {
    return retrofit.create(CampaignService.CampaignApi::class.java)
  }

  @Singleton
  @Provides
  fun provideGasService(
    @BackendBlockchainRetrofit retrofit: Retrofit
  ): GasService {
    return retrofit.create(GasService::class.java)
  }

  @Singleton
  @Provides
  fun provideAutoUpdateApi(
    @BackendLowTimerRetrofit retrofit: Retrofit
  ): AutoUpdateService.AutoUpdateApi {
    return retrofit.create(AutoUpdateService.AutoUpdateApi::class.java)
  }

  @Provides
  fun provideGamificationApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): GamificationApi {
    val gson = GsonBuilder()
      .setDateFormat("yyyy-MM-dd HH:mm")
      .registerTypeAdapter(PromotionsResponse::class.java, PromotionsSerializer())
      .registerTypeAdapter(PromotionsResponse::class.java, PromotionsDeserializer())
      .create()
    return retrofit.newBuilder()
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build()
      .create(GamificationApi::class.java)
  }

  @Singleton
  @Provides
  fun provideWithdrawApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): WithdrawApi {
    return retrofit.create(WithdrawApi::class.java)
  }
}