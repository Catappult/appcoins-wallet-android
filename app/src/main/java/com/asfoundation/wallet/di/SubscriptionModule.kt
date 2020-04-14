package com.asfoundation.wallet.di

import com.appcoins.wallet.bdsbilling.SubscriptionBillingService
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
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

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
  fun provideSubscriptionBillingService(client: OkHttpClient,
                                        gson: Gson): SubscriptionBillingService {
    return Retrofit.Builder()
        .baseUrl("http://192.168.2.3:8080/api/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(SubscriptionBillingService::class.java)
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