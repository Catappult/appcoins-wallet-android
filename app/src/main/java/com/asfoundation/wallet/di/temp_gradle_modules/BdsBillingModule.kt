package com.asfoundation.wallet.di.temp_gradle_modules

import com.appcoins.wallet.bdsbilling.*
import com.appcoins.wallet.bdsbilling.repository.*
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
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
    ewtObtainer: EwtAuthenticatorService,
    rxSchedulers: RxSchedulers,
    fiatCurrenciesPreferencesDataSource: FiatCurrenciesPreferencesDataSource
  ): BillingPaymentProofSubmission =
    BillingPaymentProofSubmissionImpl.Builder()
      .setBrokerBdsApi(brokerBdsApi)
      .setInappApi(inappApi)
      .setWalletService(walletService)
      .setSubscriptionBillingService(subscriptionBillingApi)
      .setEwtObtainer(ewtObtainer)
      .setRxSchedulers(rxSchedulers)
      .setFiatCurrenciesPreferencesDataSource(fiatCurrenciesPreferencesDataSource)
      .build()

  @Singleton
  @Provides
  fun provideRemoteRepository(
    subscriptionBillingApi: SubscriptionBillingApi,
    brokerBdsApi: BrokerBdsApi,
    inappApi: InappBillingApi,
    ewtObtainer: EwtAuthenticatorService,
    rxSchedulers: RxSchedulers,
    fiatCurrenciesPreferencesDataSource: FiatCurrenciesPreferencesDataSource
  ): RemoteRepository =
    RemoteRepository(
      brokerBdsApi,
      inappApi,
      BdsApiResponseMapper(SubscriptionsMapper(), InAppMapper()),
      subscriptionBillingApi,
      ewtObtainer,
      rxSchedulers,
      fiatCurrenciesPreferencesDataSource
    )

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