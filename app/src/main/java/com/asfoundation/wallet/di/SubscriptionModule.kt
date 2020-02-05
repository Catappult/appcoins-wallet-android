package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.service.LocalCurrencyConversionService
import com.asfoundation.wallet.subscriptions.SubscriptionApiMockedImpl
import com.asfoundation.wallet.subscriptions.SubscriptionInteract
import com.asfoundation.wallet.subscriptions.SubscriptionRepository
import com.asfoundation.wallet.subscriptions.SubscriptionService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient

@Module
class SubscriptionModule {

  @Provides
  fun provideSubscriptionService(client: OkHttpClient, gson: Gson): SubscriptionService {
    return SubscriptionApiMockedImpl()
  }

  @Provides
  fun provideSubscriptionApiMocked(): SubscriptionApiMockedImpl {
    return SubscriptionApiMockedImpl()
  }

  @Provides
  fun provideSubscriptionRepository(
      subscriptionService: SubscriptionService,
      findDefaultWalletInteract: FindDefaultWalletInteract
  ): SubscriptionRepository {
    return SubscriptionRepository(subscriptionService, findDefaultWalletInteract)
  }

  @Provides
  fun provideSubscriptionInteract(
      subscriptionRepository: SubscriptionRepository,
      localCurrencyConversionService: LocalCurrencyConversionService
  ): SubscriptionInteract {
    return SubscriptionInteract(subscriptionRepository, localCurrencyConversionService)
  }

}