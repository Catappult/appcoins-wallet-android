package com.appcoins.wallet.core.network.microservices

import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.core.network.base.annotations.BlockchainHttpClient
import com.appcoins.wallet.core.network.base.annotations.DefaultHttpClient
import com.appcoins.wallet.core.network.microservices.annotations.ProductBlockchainRetrofit
import com.appcoins.wallet.core.network.microservices.annotations.ProductDefaultRetrofit
import com.appcoins.wallet.core.network.microservices.api.InappBillingApi
import com.appcoins.wallet.core.network.microservices.api.SubscriptionBillingApi
import com.appcoins.wallet.core.network.microservices.api.TopUpValuesApi
import com.appcoins.wallet.core.network.microservices.model.FiatCurrenciesResponse
import com.appcoins.wallet.core.network.microservices.model.UserSubscriptionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
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
  ): TopUpValuesApi =
    retrofit.create(TopUpValuesApi::class.java)

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

  interface FiatCurrenciesApi {
    @GET("8.20210201/currencies?type=FIAT&icon.height=128")
    fun getFiatCurrencies(): Single<FiatCurrenciesResponse>
  }
}