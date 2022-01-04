package com.asfoundation.wallet.di.api

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.carrierbilling.CarrierBillingRepository
import com.appcoins.wallet.billing.carrierbilling.response.CarrierErrorResponse
import com.appcoins.wallet.billing.carrierbilling.response.CarrierErrorResponseTypeAdapter
import com.appcoins.wallet.billing.skills.SkillsPaymentRepository
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.billing.share.BdsShareLinkRepository
import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.topup.TopUpValuesService
import com.asfoundation.wallet.verification.ui.credit_card.network.BrokerVerificationApi
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BrokerApiModule {
  private val brokerBaseUrl = BuildConfig.BASE_HOST
  private val brokerAdyenUrl: String = "$brokerBaseUrl/broker/8.20200815/gateways/adyen_v2/"
  private val brokerCarrierBillingUrl: String = "$brokerBaseUrl/broker/8.20210329/"

  @Singleton
  @Provides
  @Named("broker-base-blockchain")
  fun provideBrokerBaseBlockchainRetrofit(@Named("blockchain") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(brokerBaseUrl)
        .build()
  }

  @Singleton
  @Provides
  @Named("broker-base-default")
  fun provideBrokerBaseDefaultRetrofit(@Named("default") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(brokerBaseUrl)
        .build()
  }

  @Singleton
  @Provides
  @Named("broker-adyen-default")
  fun provideBrokerAdyenDefaultRetrofit(@Named("default") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(brokerAdyenUrl)
        .build()
  }

  @Singleton
  @Provides
  @Named("broker-carrier-default")
  fun provideBrokerCarrierDefaultRetrofit(@Named("default") retrofit: Retrofit,
                                          rxSchedulers: RxSchedulers): Retrofit {
    val gson = GsonBuilder().registerTypeAdapter(CarrierErrorResponse::class.java,
        CarrierErrorResponseTypeAdapter())
        .create()
    return retrofit.newBuilder()
        .baseUrl(brokerCarrierBillingUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(rxSchedulers.io))
        .build()
  }

  @Singleton
  @Provides
  fun providesAdyenApi(
      @Named("broker-adyen-default") retrofit: Retrofit): AdyenPaymentRepository.AdyenApi {
    return retrofit.create(AdyenPaymentRepository.AdyenApi::class.java)
  }

  @Singleton
  @Provides
  fun providesBdsBillingApi(
      @Named("broker-base-blockchain") retrofit: Retrofit): RemoteRepository.BdsApi {
    return retrofit.create(RemoteRepository.BdsApi::class.java)
  }

  @Singleton
  @Provides
  fun providesSkillsAdyenApi(
      @Named("broker-adyen-default") retrofit: Retrofit): SkillsPaymentRepository.AdyenApi {
    return retrofit.create(SkillsPaymentRepository.AdyenApi::class.java)
  }

  @Singleton
  @Provides
  fun providesCarrierBillingApi(
      @Named("broker-carrier-default")
      retrofit: Retrofit): CarrierBillingRepository.CarrierBillingApi {
    return retrofit.create(CarrierBillingRepository.CarrierBillingApi::class.java)
  }

  @Singleton
  @Provides
  fun providesFiatCurrenciesApi(
      @Named("broker-base-default")
      retrofit: Retrofit): FiatCurrenciesRepository.FiatCurrenciesApi {
    return retrofit.create(FiatCurrenciesRepository.FiatCurrenciesApi::class.java)
  }

  @Singleton
  @Provides
  fun providesPromoCodeBrokerApi(
      @Named("broker-base-default") retrofit: Retrofit): PromoCodeRepository.PromoCodeBrokerApi {
    return retrofit.create(PromoCodeRepository.PromoCodeBrokerApi::class.java)
  }

  @Singleton
  @Provides
  fun providesTokenToLocalFiatApi(
      @Named("broker-base-default")
      retrofit: Retrofit): LocalCurrencyConversionService.TokenToLocalFiatApi {
    return retrofit.create(LocalCurrencyConversionService.TokenToLocalFiatApi::class.java)
  }

  @Singleton
  @Provides
  fun providesTopUpValuesApi(
      @Named("broker-base-default") retrofit: Retrofit): TopUpValuesService.TopUpValuesApi {
    return retrofit.create(TopUpValuesService.TopUpValuesApi::class.java)
  }

  @Singleton
  @Provides
  fun provideBdsShareLinkApi(
      @Named("broker-base-default") retrofit: Retrofit): BdsShareLinkRepository.BdsShareLinkApi {
    return retrofit.create(BdsShareLinkRepository.BdsShareLinkApi::class.java)
  }

  @Singleton
  @Provides
  fun provideWalletValidationApi(
      @Named("broker-adyen-default") retrofit: Retrofit): BrokerVerificationApi {
    return retrofit.create(BrokerVerificationApi::class.java)
  }
}