package com.asfoundation.wallet.di.api

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.skills.SkillsPaymentRepository
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.change_currency.FiatCurrenciesRepository
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BrokerApiModule {
  private val brokerBaseUrl = BuildConfig.BASE_HOST
  private val brokerAdyenUrl: String = "$brokerBaseUrl/broker/8.20200815/gateways/adyen_v2/"
  private val baseCarrierBillingUrl: String = "$brokerBaseUrl/broker/8.20210329/"

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
}