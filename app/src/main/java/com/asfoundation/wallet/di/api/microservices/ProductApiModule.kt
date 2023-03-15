package com.asfoundation.wallet.di.api.microservices

import com.appcoins.wallet.bdsbilling.repository.InappBillingApi
import com.appcoins.wallet.bdsbilling.repository.SubscriptionBillingApi
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.core.network.base.annotations.BlockchainHttpClient
import com.appcoins.wallet.core.network.base.annotations.DefaultHttpClient
import com.asfoundation.wallet.di.annotations.*
import com.asfoundation.wallet.subscriptions.UserSubscriptionApi
import com.asfoundation.wallet.topup.TopUpValuesService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ProductApiModule {

  private val productUrl = "${HostProperties.MS_HOST}/productv2/"

  @Singleton
  @Provides
  @ProductBlockchainRetrofit
  fun provideSubscriptionsBlockchainRetrofit(@BlockchainHttpClient client: OkHttpClient): Retrofit =
    Retrofit.Builder()
      .baseUrl(productUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()

  @Singleton
  @Provides
  @ProductDefaultRetrofit
  fun provideSubscriptionsDefaultRetrofit(@DefaultHttpClient client: OkHttpClient): Retrofit =
    Retrofit.Builder()
      .baseUrl(productUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()

  @Singleton
  @Provides
  fun providesTopUpValuesApi(
    @ProductDefaultRetrofit retrofit: Retrofit
  ): TopUpValuesService.TopUpValuesApi =
    retrofit.create(TopUpValuesService.TopUpValuesApi::class.java)

  @Singleton
  @Provides
  fun providesInappBillingApi(
    @ProductBlockchainRetrofit retrofit: Retrofit
  ): InappBillingApi = retrofit.create(InappBillingApi::class.java)

  @Provides
  fun providesSubscriptionBillingApi(
    @ProductBlockchainRetrofit retrofit: Retrofit
  ): SubscriptionBillingApi =
    retrofit.create(SubscriptionBillingApi::class.java)

  @Provides
  fun providesUserSubscriptionApi(
    @ProductDefaultRetrofit retrofit: Retrofit
  ): UserSubscriptionApi =
    retrofit.create(UserSubscriptionApi::class.java)
}