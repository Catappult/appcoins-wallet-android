package com.asfoundation.wallet.di.temp_gradle_modules

import com.appcoins.wallet.bdsbilling.*
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.*
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
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
  fun providesBillingPaymentProofSubmission(brokerBdsApi: RemoteRepository.BrokerBdsApi,
                                            inappBdsApi: RemoteRepository.InappBdsApi,
                                            walletService: WalletService,
                                            subscriptionBillingApi: SubscriptionBillingApi,
                                            bdsApi: BdsApiSecondary,
                                            billingSerializer: ExternalBillingSerializer): BillingPaymentProofSubmission {
    return BillingPaymentProofSubmissionImpl.Builder()
        .setBrokerBdsApi(brokerBdsApi)
        .setInappBdsApi(inappBdsApi)
        .setBillingSerializer(billingSerializer)
        .setBdsApiSecondary(bdsApi)
        .setWalletService(walletService)
        .setSubscriptionBillingService(subscriptionBillingApi)
        .build()
  }


  @Singleton
  @Provides
  fun provideBillingFactory(walletService: WalletService, bdsRepository: BdsRepository): Billing {
    return BdsBilling(bdsRepository, walletService, BillingThrowableCodeMapper())
  }

  @Singleton
  @Provides
  fun provideBillingSerializer() = ExternalBillingSerializer()

  @Singleton
  @Provides
  fun provideRemoteRepository(subscriptionBillingApi: SubscriptionBillingApi,
                              brokerBdsApi: RemoteRepository.BrokerBdsApi,
                              inappBdsApi: RemoteRepository.InappBdsApi,
                              api: BdsApiSecondary): RemoteRepository {
    return RemoteRepository(brokerBdsApi,inappBdsApi,
        BdsApiResponseMapper(SubscriptionsMapper(), InAppMapper(ExternalBillingSerializer())), api,
        subscriptionBillingApi, ExternalBillingSerializer())
  }

  @Singleton
  @Provides
  fun provideBdsRepository(repository: RemoteRepository) = BdsRepository(repository)

  @Singleton
  @Provides
  fun provideProxyService(proxySdk: AppCoinsAddressProxySdk): ProxyService {
    return object : ProxyService {
      private val NETWORK_ID_ROPSTEN = 3
      private val NETWORK_ID_MAIN = 1
      override fun getAppCoinsAddress(debug: Boolean): Single<String> {
        return proxySdk.getAppCoinsAddress(if (debug) NETWORK_ID_ROPSTEN else NETWORK_ID_MAIN)
      }

      override fun getIabAddress(debug: Boolean): Single<String> {
        return proxySdk.getIabAddress(if (debug) NETWORK_ID_ROPSTEN else NETWORK_ID_MAIN)
      }
    }
  }
}