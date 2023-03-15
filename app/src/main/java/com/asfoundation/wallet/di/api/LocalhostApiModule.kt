package com.asfoundation.wallet.di.api

import com.appcoins.wallet.core.network.base.annotations.DefaultHttpClient
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
class LocalhostApiModule {

  private val localhost = "https://localhost/"

  @Singleton
  @Provides
  @Named("localhost-default")
  fun provideLocalhostDefaultRetrofit(@DefaultHttpClient client : OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(localhost)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

}