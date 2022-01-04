package com.asfoundation.wallet.di.api

import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.subscriptions.UserSubscriptionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class SubscriptionsApiModule {

  private val subsBaseUrl = BuildConfig.SUBS_BASE_HOST
  private val subscriptionsUrl = "$subsBaseUrl/productv2/8.20200701/applications/"

  @Singleton
  @Provides
  @Named("subscriptions-blockchain")
  fun provideSubscriptionsBlockchainRetrofit(@Named("blockchain") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(subscriptionsUrl)
        .build()
  }

  @Singleton
  @Provides
  @Named("subscriptions-default")
  fun provideSubscriptionsDefaultRetrofit(@Named("default") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(subscriptionsUrl)
        .build()
  }

  @Provides
  fun providesSubscriptionBillingApi(
      @Named("subscriptions-blockchain") retrofit: Retrofit): SubscriptionBillingApi {
    return retrofit.create(SubscriptionBillingApi::class.java)
  }

  @Provides
  fun providesUserSubscriptionApi(
      @Named("subscriptions-default") retrofit: Retrofit): UserSubscriptionApi {
    return retrofit.create(UserSubscriptionApi::class.java)
  }
}