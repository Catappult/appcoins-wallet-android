package com.appcoins.wallet.core.network.bds

import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.core.network.base.annotations.DefaultHttpClient
import com.appcoins.wallet.core.network.bds.api.BdsApiSecondary
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BdsApiModule {

  private val bdsUrl = HostProperties.WS75_HOST

  @Singleton
  @Provides
  @Named("bds-default")
  fun provideBdsDefaultRetrofit(@DefaultHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(bdsUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  fun provideBdsApiSecondary(
      @Named("bds-default") retrofit: Retrofit): BdsApiSecondary {
    return retrofit.create(BdsApiSecondary::class.java)
  }
}