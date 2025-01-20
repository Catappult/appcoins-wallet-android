package com.appcoins.wallet.core.network.microservices

import com.appcoins.wallet.core.network.base.annotations.BlockchainHttpClient
import com.appcoins.wallet.core.network.microservices.annotations.PayFlowRetrofit
import com.appcoins.wallet.core.network.microservices.api.payflow.PayFlowApi
import com.appcoins.wallet.core.utils.properties.HostProperties
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
class PayFlowApiModule {

  private val PayFlowUrl = "${HostProperties.PAY_FLOW_HOST}/"

  @Singleton
  @Provides
  @PayFlowRetrofit
  fun providePayFlowRetrofit(@BlockchainHttpClient client: OkHttpClient): Retrofit =
    Retrofit.Builder()
      .baseUrl(PayFlowUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()


  @Singleton
  @Provides
  fun providesPayFlowApi(
    @PayFlowRetrofit retrofit: Retrofit
  ): PayFlowApi = retrofit.create(PayFlowApi::class.java)

}