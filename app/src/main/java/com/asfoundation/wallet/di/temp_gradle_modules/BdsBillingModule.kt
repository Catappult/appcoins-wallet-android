package com.asfoundation.wallet.di.temp_gradle_modules

import com.appcoins.wallet.bdsbilling.BdsBilling
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmission
import com.appcoins.wallet.bdsbilling.BillingPaymentProofSubmissionImpl
import com.appcoins.wallet.bdsbilling.BillingThrowableCodeMapper
import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.bdsbilling.repository.BdsApiResponseMapper
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.InAppMapper
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.SubscriptionsMapper
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.product.InappBillingApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.properties.MiscProperties
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.sharedpreferences.FiatCurrenciesPreferencesDataSource
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxySdk
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.Single
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BdsBillingModule {
  @Singleton
  @Provides
  fun providesBillingPaymentProofSubmission(
    brokerBdsApi: BrokerBdsApi,
    inappApi: InappBillingApi,
    walletService: WalletService,
    subscriptionBillingApi: SubscriptionBillingApi,
    rxSchedulers: RxSchedulers,
    fiatCurrenciesPreferencesDataSource: FiatCurrenciesPreferencesDataSource
  ): BillingPaymentProofSubmission =
    BillingPaymentProofSubmissionImpl.Builder()
      .setBrokerBdsApi(brokerBdsApi)
      .setInappApi(inappApi)
      .setWalletService(walletService)
      .setSubscriptionBillingService(subscriptionBillingApi)
      .setRxSchedulers(rxSchedulers)
      .setFiatCurrenciesPreferencesDataSource(fiatCurrenciesPreferencesDataSource)
      .build()


  @Singleton
  @Provides
  fun provideBillingFactory(
    walletService: WalletService,
    bdsRepository: BdsRepository,
    partnerAddressService: PartnerAddressService
  ): Billing =
    BdsBilling(
      repository = bdsRepository,
      walletService = walletService,
      errorMapper = BillingThrowableCodeMapper(),
      partnerAddressService = partnerAddressService
    )

  @Singleton
  @Provides
  fun provideRemoteRepository(
    subscriptionBillingApi: SubscriptionBillingApi,
    brokerBdsApi: BrokerBdsApi,
    inappApi: InappBillingApi,
    rxSchedulers: RxSchedulers,
    fiatCurrenciesPreferencesDataSource: FiatCurrenciesPreferencesDataSource
  ): RemoteRepository =
    RemoteRepository(
      brokerBdsApi = brokerBdsApi,
      inappApi = inappApi,
      responseMapper = BdsApiResponseMapper(SubscriptionsMapper(), InAppMapper()),
      subsApi = subscriptionBillingApi,
      rxSchedulers = rxSchedulers,
      fiatCurrenciesPreferences = fiatCurrenciesPreferencesDataSource
    )

  @Singleton
  @Provides
  fun provideBdsRepository(repository: RemoteRepository) = BdsRepository(repository)

  @Singleton
  @Provides
  fun provideProxyService(proxySdk: AppCoinsAddressProxySdk): ProxyService =
    object : ProxyService {
      override fun getAppCoinsAddress(debug: Boolean): Single<String> =
        proxySdk.getAppCoinsAddress(MiscProperties.NETWORK_ID)

      override fun getIabAddress(debug: Boolean): Single<String> =
        proxySdk.getIabAddress(MiscProperties.NETWORK_ID)
    }
}