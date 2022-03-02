package com.asfoundation.wallet.di.api.microservices

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.di.annotations.BlockchainHttpClient
import com.asfoundation.wallet.di.annotations.BrokerBlockchainRetrofit
import com.asfoundation.wallet.di.annotations.InappBlockchainRetrofit
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
class InappApiModule {
  private val inappUrl: String = "${BuildConfig.BASE_HOST}/inapp/"

  @Singleton
  @Provides
  @InappBlockchainRetrofit
  fun provideBrokerBlockchainRetrofit(@BlockchainHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(inappUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  fun providesInappBdsApi(
    @InappBlockchainRetrofit retrofit: Retrofit
  ): RemoteRepository.InappBdsApi {
    return retrofit.create(RemoteRepository.InappBdsApi::class.java)
  }

}