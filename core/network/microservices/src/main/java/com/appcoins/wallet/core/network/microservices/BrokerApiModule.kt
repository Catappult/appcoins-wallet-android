package com.appcoins.wallet.core.network.microservices

import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.core.network.base.annotations.BlockchainHttpClient
import com.appcoins.wallet.core.network.base.annotations.DefaultHttpClient
import com.appcoins.wallet.core.network.microservices.ProductApiModule.FiatCurrenciesApi
import com.appcoins.wallet.core.network.microservices.annotations.BrokerBlockchainRetrofit
import com.appcoins.wallet.core.network.microservices.annotations.BrokerDefaultRetrofit
import com.appcoins.wallet.core.network.microservices.api.BackupEmailApi
import com.appcoins.wallet.core.network.microservices.api.BrokerVerificationApi
import com.appcoins.wallet.core.network.microservices.api.BrokerVerificationApi.*
import com.appcoins.wallet.core.network.microservices.api.TopUpApi
import com.appcoins.wallet.core.network.microservices.model.CarrierErrorResponse
import com.appcoins.wallet.core.network.microservices.model.CarrierErrorResponseTypeAdapter
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BrokerApiModule {

  private val brokerUrl = "${HostProperties.MS_HOST}/broker/"

  @Singleton
  @Provides
  @BrokerBlockchainRetrofit
  fun provideBrokerBlockchainRetrofit(@BlockchainHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(brokerUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  @BrokerDefaultRetrofit
  fun provideBrokerDefaultRetrofit(@DefaultHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(brokerUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  fun providesAdyenApi(
    @BrokerDefaultRetrofit retrofit: Retrofit
  ): AdyenApi {
    return retrofit.create(AdyenApi::class.java)
  }

  @Singleton
  @Provides
  fun providesPaypalApi(
    @BrokerDefaultRetrofit retrofit: Retrofit
  ): PaypalV2Api {
    return retrofit.create(PaypalV2Api::class.java)
  }

  @Singleton
  @Provides
  fun providesBrokerBdsApi(
    @BrokerBlockchainRetrofit retrofit: Retrofit
  ): BrokerBdsApi {
    return retrofit.create(BrokerBdsApi::class.java)
  }

  @Provides
  fun providesCarrierBillingApi1(
    @DefaultHttpClient client: OkHttpClient,
    @BrokerDefaultRetrofit retrofit: Retrofit,
    rxSchedulers: RxSchedulers
  ): CarrierBillingApi {
    val gson = GsonBuilder().registerTypeAdapter(
      CarrierErrorResponse::class.java,
      CarrierErrorResponseTypeAdapter()
    ).create()
    return Retrofit.Builder()
      .baseUrl(brokerUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(rxSchedulers.io))
      .build()
      .create(CarrierBillingApi::class.java)
  }

  @Singleton
  @Provides
  fun providesFiatCurrenciesApi(
    @BrokerDefaultRetrofit retrofit: Retrofit
  ): FiatCurrenciesApi {
    return retrofit.create(FiatCurrenciesApi::class.java)
  }

  @Singleton
  @Provides
  fun providesTokenToLocalFiatApi(
    @BrokerDefaultRetrofit retrofit: Retrofit
  ): TokenToLocalFiatApi {
    return retrofit.create(TokenToLocalFiatApi::class.java)
  }

  @Singleton
  @Provides
  fun provideWalletValidationApi(
    @BrokerDefaultRetrofit retrofit: Retrofit
  ): BrokerVerificationApi {
    return retrofit.create(BrokerVerificationApi::class.java)
  }

  @Singleton
  @Provides
  fun providesBackupEmailApi(
    @BrokerDefaultRetrofit retrofit: Retrofit
  ): BackupEmailApi {
    return retrofit.create(BackupEmailApi::class.java)
  }

  @Singleton
  @Provides
  fun providesTopUpApi(
          @BrokerDefaultRetrofit retrofit: Retrofit
  ): TopUpApi {
    return retrofit.create(TopUpApi::class.java)
  }
}