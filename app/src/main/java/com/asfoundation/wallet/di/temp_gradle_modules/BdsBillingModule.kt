package com.asfoundation.wallet.di.temp_gradle_modules

import com.appcoins.wallet.bdsbilling.*
import com.appcoins.wallet.bdsbilling.repository.*
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.bds.api.BdsApiSecondary
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.product.InappBillingApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.properties.MiscProperties
import com.appcoins.wallet.core.walletservices.WalletService
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
    bdsApi: BdsApiSecondary,
    ewtObtainer: EwtAuthenticatorService,
    rxSchedulers: RxSchedulers
  ): BillingPaymentProofSubmission =
    BillingPaymentProofSubmissionImpl.Builder()
      .setBrokerBdsApi(brokerBdsApi)
      .setInappApi(inappApi)
      .setBdsApiSecondary(bdsApi)
      .setWalletService(walletService)
      .setSubscriptionBillingService(subscriptionBillingApi)
      .setEwtObtainer(ewtObtainer)
      .setRxSchedulers(rxSchedulers)
      .build()


  @Singleton
  @Provides
  fun provideBillingFactory(
    walletService: WalletService,
    bdsRepository: BdsRepository,
    partnerAddressService: PartnerAddressService
  ): Billing =
    BdsBilling(bdsRepository, walletService, BillingThrowableCodeMapper(), partnerAddressService)

  @Singleton
  @Provides
  fun provideRemoteRepository(
    subscriptionBillingApi: SubscriptionBillingApi,
    brokerBdsApi: BrokerBdsApi,
    inappApi: InappBillingApi,
    api: BdsApiSecondary,
    ewtObtainer: EwtAuthenticatorService,
    rxSchedulers: RxSchedulers
  ): RemoteRepository =
    RemoteRepository(
      brokerBdsApi,
      inappApi,
      BdsApiResponseMapper(SubscriptionsMapper(), InAppMapper()),
      api,
      subscriptionBillingApi,
      ewtObtainer,
      rxSchedulers
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