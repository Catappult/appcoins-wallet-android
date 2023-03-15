package com.asfoundation.wallet.di.temp_gradle_modules

import com.appcoins.wallet.bdsbilling.*
import com.appcoins.wallet.bdsbilling.repository.*
import com.appcoins.wallet.core.utils.properties.MiscProperties
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
    brokerBdsApi: RemoteRepository.BrokerBdsApi,
    inappApi: InappBillingApi,
    walletService: WalletService,
    subscriptionBillingApi: SubscriptionBillingApi,
    bdsApi: BdsApiSecondary
  ): BillingPaymentProofSubmission =
    BillingPaymentProofSubmissionImpl.Builder()
      .setBrokerBdsApi(brokerBdsApi)
      .setInappApi(inappApi)
      .setBdsApiSecondary(bdsApi)
      .setWalletService(walletService)
      .setSubscriptionBillingService(subscriptionBillingApi)
      .build()


  @Singleton
  @Provides
  fun provideBillingFactory(walletService: WalletService, bdsRepository: BdsRepository): Billing =
    BdsBilling(bdsRepository, walletService, BillingThrowableCodeMapper())

  @Singleton
  @Provides
  fun provideRemoteRepository(
    subscriptionBillingApi: SubscriptionBillingApi,
    brokerBdsApi: RemoteRepository.BrokerBdsApi,
    inappApi: InappBillingApi,
    api: BdsApiSecondary
  ): RemoteRepository =
    RemoteRepository(
      brokerBdsApi,
      inappApi,
      BdsApiResponseMapper(SubscriptionsMapper(), InAppMapper()),
      api,
      subscriptionBillingApi
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