package com.asfoundation.wallet.di.api.microservices

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.skills.SkillsPaymentRepository
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.billing.carrier_billing.CarrierBillingRepository
import com.asfoundation.wallet.billing.carrier_billing.CarrierErrorResponse
import com.asfoundation.wallet.billing.carrier_billing.CarrierErrorResponseTypeAdapter
import com.asfoundation.wallet.billing.share.BdsShareLinkRepository
import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import com.asfoundation.wallet.di.annotations.BlockchainHttpClient
import com.asfoundation.wallet.di.annotations.BrokerBlockchainRetrofit
import com.asfoundation.wallet.di.annotations.BrokerDefaultRetrofit
import com.asfoundation.wallet.di.annotations.DefaultHttpClient
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.verification.repository.BrokerVerificationRepository
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

  private val brokerUrl = "${BuildConfig.BASE_HOST}/broker/"

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
  ): AdyenPaymentRepository.AdyenApi {
    return retrofit.create(AdyenPaymentRepository.AdyenApi::class.java)
  }

  @Singleton
  @Provides
  fun providesBrokerBdsApi(
    @BrokerBlockchainRetrofit retrofit: Retrofit
  ): RemoteRepository.BrokerBdsApi {
    return retrofit.create(RemoteRepository.BrokerBdsApi::class.java)
  }

  @Singleton
  @Provides
  fun providesSkillsAdyenApi(
    @BrokerDefaultRetrofit retrofit: Retrofit
  ): SkillsPaymentRepository.AdyenApi {
    return retrofit.create(SkillsPaymentRepository.AdyenApi::class.java)
  }

  @Provides
  fun providesCarrierBillingApi1(
    @DefaultHttpClient client: OkHttpClient,
    @BrokerDefaultRetrofit retrofit: Retrofit,
    rxSchedulers: RxSchedulers
  ): CarrierBillingRepository.CarrierBillingApi {
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
      .create(CarrierBillingRepository.CarrierBillingApi::class.java)
  }

  @Singleton
  @Provides
  fun providesFiatCurrenciesApi(
    @BrokerDefaultRetrofit retrofit: Retrofit
  ): FiatCurrenciesRepository.FiatCurrenciesApi {
    return retrofit.create(FiatCurrenciesRepository.FiatCurrenciesApi::class.java)
  }

  @Singleton
  @Provides
  fun providesPromoCodeBrokerApi(
    @BrokerDefaultRetrofit retrofit: Retrofit
  ): PromoCodeRepository.PromoCodeBrokerApi {
    return retrofit.create(PromoCodeRepository.PromoCodeBrokerApi::class.java)
  }

  @Singleton
  @Provides
  fun providesTokenToLocalFiatApi(
    @BrokerDefaultRetrofit retrofit: Retrofit
  ): LocalCurrencyConversionService.TokenToLocalFiatApi {
    return retrofit.create(LocalCurrencyConversionService.TokenToLocalFiatApi::class.java)
  }

  @Singleton
  @Provides
  fun provideWalletValidationApi(
    @BrokerDefaultRetrofit retrofit: Retrofit
  ): BrokerVerificationRepository.BrokerVerificationApi {
    return retrofit.create(BrokerVerificationRepository.BrokerVerificationApi::class.java)
  }
}