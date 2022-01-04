package com.asfoundation.wallet.di.temp_gradle_modules

import com.appcoins.wallet.bdsbilling.*
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.*
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BdsBillingModule {
  @Singleton
  @Provides
  fun providesBillingPaymentProofSubmission(api: RemoteRepository.BdsApi,
                                            walletService: WalletService,
                                            subscriptionBillingApi: SubscriptionBillingApi,
                                            bdsApi: BdsApiSecondary,
                                            billingSerializer: ExternalBillingSerializer): BillingPaymentProofSubmission {
    return BillingPaymentProofSubmissionImpl.Builder()
        .setApi(api)
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
                              bdsApi: RemoteRepository.BdsApi,
                              api: BdsApiSecondary): RemoteRepository {
    return RemoteRepository(bdsApi,
        BdsApiResponseMapper(SubscriptionsMapper(), InAppMapper(ExternalBillingSerializer())), api,
        subscriptionBillingApi, ExternalBillingSerializer())
  }

  @Singleton
  @Provides
  fun provideBdsRepository(repository: RemoteRepository) = BdsRepository(repository)
}