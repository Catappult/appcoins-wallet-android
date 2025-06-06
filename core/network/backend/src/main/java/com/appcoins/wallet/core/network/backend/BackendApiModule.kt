package com.appcoins.wallet.core.network.backend

import com.appcoins.wallet.core.network.backend.annotations.BackendBlockchainRetrofit
import com.appcoins.wallet.core.network.backend.annotations.BackendDefaultRetrofit
import com.appcoins.wallet.core.network.backend.annotations.BackendShortTimeoutRetrofit
import com.appcoins.wallet.core.network.backend.api.AutoUpdateApi
import com.appcoins.wallet.core.network.backend.api.BackupLogApi
import com.appcoins.wallet.core.network.backend.api.CachedBackupApi
import com.appcoins.wallet.core.network.backend.api.CachedGuestWalletApi
import com.appcoins.wallet.core.network.backend.api.CachedTransactionApi
import com.appcoins.wallet.core.network.backend.api.CountryApi
import com.appcoins.wallet.core.network.backend.api.GamesApi
import com.appcoins.wallet.core.network.backend.api.GamificationApi
import com.appcoins.wallet.core.network.backend.api.GasServiceApi
import com.appcoins.wallet.core.network.backend.api.ImpressionApi
import com.appcoins.wallet.core.network.backend.api.LoginApi
import com.appcoins.wallet.core.network.backend.api.PartnerAttributionApi
import com.appcoins.wallet.core.network.backend.api.PromoCodeApi
import com.appcoins.wallet.core.network.backend.api.RedeemGiftApi
import com.appcoins.wallet.core.network.backend.api.SupportApi
import com.appcoins.wallet.core.network.backend.api.TokenToFiatApi
import com.appcoins.wallet.core.network.backend.api.TransactionOverviewApi
import com.appcoins.wallet.core.network.backend.api.TransactionsApi
import com.appcoins.wallet.core.network.backend.api.WalletInfoApi
import com.appcoins.wallet.core.network.backend.model.PromotionsDeserializer
import com.appcoins.wallet.core.network.backend.model.PromotionsResponse
import com.appcoins.wallet.core.network.backend.model.PromotionsSerializer
import com.appcoins.wallet.core.network.base.annotations.BlockchainHttpClient
import com.appcoins.wallet.core.network.base.annotations.DefaultHttpClient
import com.appcoins.wallet.core.network.base.annotations.RenewJwtBackendUrl
import com.appcoins.wallet.core.network.base.annotations.ShortTimeoutHttpClient
import com.appcoins.wallet.core.network.base.compat.EmailApi
import com.appcoins.wallet.core.utils.properties.HostProperties
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
import java.util.Locale
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BackendApiModule {

  private val backendUrl = HostProperties.BACKEND_HOST

  @Provides
  @RenewJwtBackendUrl
  fun provideBackendUrl(): String = backendUrl

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
  ): CountryApi {
    return retrofit.create(CountryApi::class.java)
  }

  @Singleton
  @Provides
  fun providesOffChainTransactionsApi(
    @BlockchainHttpClient client: OkHttpClient
  ): TransactionsApi {
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
      .create(TransactionsApi::class.java)
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
  ): PromoCodeApi {
    return retrofit.create(PromoCodeApi::class.java)
  }

  @Singleton
  @Provides
  fun providesRedeemGiftBackendApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): RedeemGiftApi {
    return retrofit.create(RedeemGiftApi::class.java)
  }

  @Singleton
  @Provides
  fun providesWalletInfoApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): WalletInfoApi {
    return retrofit.create(WalletInfoApi::class.java)
  }

  @Singleton
  @Provides
  fun providesCachedTransactionApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): CachedTransactionApi {
    return retrofit.create(CachedTransactionApi::class.java)
  }

  @Singleton
  @Provides
  fun providesCachedBackupApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): CachedBackupApi {
    return retrofit.create(CachedBackupApi::class.java)
  }

  @Singleton
  @Provides
  fun providesBackupLogApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): BackupLogApi {
    return retrofit.create(BackupLogApi::class.java)
  }

  @Singleton
  @Provides
  fun provideTokenToFiatApi(
    @BackendBlockchainRetrofit retrofit: Retrofit
  ): TokenToFiatApi {
    return retrofit.create(TokenToFiatApi::class.java)
  }

  @Singleton
  @Provides
  fun provideGasService(
    @BackendBlockchainRetrofit retrofit: Retrofit
  ): GasServiceApi {
    return retrofit.create(GasServiceApi::class.java)
  }

  @Singleton
  @Provides
  fun provideAutoUpdateApi(
    @BackendShortTimeoutRetrofit retrofit: Retrofit
  ): AutoUpdateApi {
    return retrofit.create(AutoUpdateApi::class.java)
  }

  @Singleton
  @Provides
  fun provideCachedGuestWalletApi(
    @BackendShortTimeoutRetrofit retrofit: Retrofit
  ): CachedGuestWalletApi {
    return retrofit.create(CachedGuestWalletApi::class.java)
  }

  @Singleton
  @Provides
  fun provideTransactionApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): TransactionOverviewApi {
    return retrofit.create(TransactionOverviewApi::class.java)
  }

  @Singleton
  @Provides
  fun providesGamesApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): GamesApi {
    return retrofit.create(GamesApi::class.java)
  }

  @Singleton
  @Provides
  fun providesPartnerAttributionApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): PartnerAttributionApi {
    return retrofit.create(PartnerAttributionApi::class.java)
  }

  @Singleton
  @Provides
  fun providesSupportApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): SupportApi {
    return retrofit.create(SupportApi::class.java)
  }

  @Singleton
  @Provides
  fun providesEmailApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): EmailApi {
    return retrofit.create(EmailApi::class.java)
  }

  @Singleton
  @Provides
  fun provideImpressionApi(
    @BackendShortTimeoutRetrofit retrofit: Retrofit
  ): ImpressionApi {
    return retrofit.create(ImpressionApi::class.java)
  }

  @Singleton
  @Provides
  fun providesLoginApi(
    @BackendDefaultRetrofit retrofit: Retrofit
  ): LoginApi {
    return retrofit.create(LoginApi::class.java)
  }

}