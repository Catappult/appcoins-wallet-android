package com.asfoundation.wallet.di.api

import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.gamification.repository.GamificationApi
import com.appcoins.wallet.gamification.repository.entity.PromotionsDeserializer
import com.appcoins.wallet.gamification.repository.entity.PromotionsResponse
import com.appcoins.wallet.gamification.repository.entity.PromotionsSerializer
import com.asfoundation.wallet.backup.repository.BackupRepository
import com.asfoundation.wallet.di.annotations.*
import com.asfoundation.wallet.eskills.withdraw.repository.WithdrawApi
import com.asfoundation.wallet.nfts.repository.NftApi
import com.asfoundation.wallet.onboarding.CachedTransactionRepository
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import com.asfoundation.wallet.redeem_gift.repository.RedeemGiftRepository
import com.asfoundation.wallet.repository.IpCountryCodeProvider
import com.asfoundation.wallet.repository.OffChainTransactionsRepository
import com.asfoundation.wallet.service.AutoUpdateService
import com.asfoundation.wallet.service.GasService
import com.asfoundation.wallet.service.TokenRateService
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BackendApiModule {

  private val backendUrl = HostProperties.BACKEND_HOST

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
      .addConverterFactory(
        GsonConverterFactory.create(
          GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create()
        )
      )
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  @BackendShortTimeoutRetrofit
  fun provideBackendShortTimeoutRetrofit(@ShortTimeoutHttpClient client: OkHttpClient): Retrofit {
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
    @BlockchainHttpClient client: OkHttpClient
  ): OffChainTransactionsRepository.TransactionsApi {
    val objectMapper = ObjectMapper()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    objectMapper.dateFormat = dateFormat
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    return Retrofit.Builder()
      .baseUrl(backendUrl)
      .client(client)
      .addConverterFactory(JacksonConverterFactory.create(objectMapper))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
      .create(OffChainTransactionsRepository.TransactionsApi::class.java)
  }

  @Provides
  fun provideGamificationApi(
    @DefaultHttpClient client: OkHttpClient
  ): GamificationApi {
    val gson = GsonBuilder()
      .setDateFormat("yyyy-MM-dd HH:mm")
      .registerTypeAdapter(PromotionsResponse::class.java, PromotionsSerializer())
      .registerTypeAdapter(PromotionsResponse::class.java, PromotionsDeserializer())
      .create()
    return Retrofit.Builder()
      .baseUrl(backendUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
      .create(GamificationApi::class.java)
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
  fun providesRedeemGiftBackendApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): RedeemGiftRepository.RedeemGiftBackendApi {
    return retrofit.create(RedeemGiftRepository.RedeemGiftBackendApi::class.java)
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
  fun providesCachedTransactionApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): CachedTransactionRepository.CachedTransactionApi {
    return retrofit.create(CachedTransactionRepository.CachedTransactionApi::class.java)
  }

  @Singleton
  @Provides
  fun providesBackupLogApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): BackupRepository.BackupLogApi {
    return retrofit.create(BackupRepository.BackupLogApi::class.java)
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
  fun provideGasService(
    @BackendBlockchainRetrofit retrofit: Retrofit
  ): GasService {
    return retrofit.create(GasService::class.java)
  }

  @Singleton
  @Provides
  fun provideAutoUpdateApi(
    @BackendShortTimeoutRetrofit retrofit: Retrofit
  ): AutoUpdateService.AutoUpdateApi {
    return retrofit.create(AutoUpdateService.AutoUpdateApi::class.java)
  }

  @Singleton
  @Provides
  fun provideWithdrawApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): WithdrawApi {
    return retrofit.create(WithdrawApi::class.java)
  }
}