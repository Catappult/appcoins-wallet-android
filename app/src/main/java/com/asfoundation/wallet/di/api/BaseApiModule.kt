package com.asfoundation.wallet.di.api

import android.content.Context
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.util.LogInterceptor
import com.asfoundation.wallet.util.UserAgentInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BaseApiModule {

  @Singleton
  @Provides
  @Named("base")
  fun provideOkHttpClient(@ApplicationContext context: Context,
                          preferencesRepositoryType: PreferencesRepositoryType,
                          logInterceptor: LogInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor(context, preferencesRepositoryType))
        .addInterceptor(logInterceptor)
        .build()
  }

  @Singleton
  @Provides
  @Named("blockchain")
  fun provideBlockchainOkHttpClient(@Named("base") client: OkHttpClient): OkHttpClient {
    return client.newBuilder()
        .connectTimeout(15, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.MINUTES)
        .writeTimeout(30, TimeUnit.MINUTES)
        .build()
  }

  @Singleton
  @Provides
  @Named("default")
  fun provideDefaultOkHttpClient(@Named("base") client: OkHttpClient): OkHttpClient {
    return client.newBuilder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
  }

  @Singleton
  @Provides
  @Named("low-timer")
  fun provideLowTimerOkHttpClient(@Named("base") client: OkHttpClient): OkHttpClient {
    return client.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()
  }

  @Singleton
  @Provides
  @Named("blockchain")
  fun provideBlockchainRetrofit(@Named("blockchain") client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
  }

  @Singleton
  @Provides
  @Named("default")
  fun provideDefaultRetrofit(@Named("default") client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
  }

  @Singleton
  @Provides
  @Named("low-timer")
  fun provideLowTimerRetrofit(@Named("low-timer") client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
  }
}