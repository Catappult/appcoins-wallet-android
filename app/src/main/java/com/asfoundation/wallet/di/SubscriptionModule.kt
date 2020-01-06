package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.service.LocalCurrencyConversionService
import com.asfoundation.wallet.subscriptions.*
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
  fun provideSubscriptionApi(client: OkHttpClient, gson: Gson): SubscriptionApi {
    return Retrofit.Builder()
        .baseUrl("http://google.pt")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(SubscriptionApi::class.java)
  }

  @Provides
  fun provideSubscriptionApiMocked(): SubscriptionApiMocked {
    return SubscriptionApiMockedImpl()
  }

  @Provides
  fun provideSubscriptionRepository(
      subscriptionApi: SubscriptionApi,
      subscriptionApiMocked: SubscriptionApiMocked,
      findDefaultWalletInteract: FindDefaultWalletInteract
  ): SubscriptionRepository {
    return SubscriptionRepository(subscriptionApi, subscriptionApiMocked, findDefaultWalletInteract)
  }

  @Provides
  fun provideSubscriptionInteract(
      subscriptionRepository: SubscriptionRepository,
      localCurrencyConversionService: LocalCurrencyConversionService
  ): SubscriptionInteract {
    return SubscriptionInteract(subscriptionRepository, localCurrencyConversionService)
  }

}