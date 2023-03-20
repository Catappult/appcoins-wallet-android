package com.appcoins.wallet.core.network.base

import android.content.Context
import com.appcoins.wallet.core.network.base.annotations.BaseHttpClient
import com.appcoins.wallet.core.network.base.annotations.BlockchainHttpClient
import com.appcoins.wallet.core.network.base.annotations.DefaultHttpClient
import com.appcoins.wallet.core.network.base.annotations.ShortTimeoutHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.appcoins.wallet.core.network.base.interceptors.LogInterceptor
import com.appcoins.wallet.core.network.base.interceptors.MagnesHeaderInterceptor
import com.appcoins.wallet.core.network.base.interceptors.UserAgentInterceptor
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BaseApiModule {

  @Singleton
  @Provides
  @BaseHttpClient
  fun provideOkHttpClient(
    @ApplicationContext context: Context,
    commonsPreferencesDataSource: CommonsPreferencesDataSource,
    logInterceptor: LogInterceptor
  ): OkHttpClient {
    return OkHttpClient.Builder()
      .addInterceptor(UserAgentInterceptor(context, commonsPreferencesDataSource))
      .addInterceptor(MagnesHeaderInterceptor(context))
      .addInterceptor(logInterceptor)
      .build()
  }

  @Singleton
  @Provides
  @BlockchainHttpClient
  fun provideBlockchainOkHttpClient(@BaseHttpClient client: OkHttpClient): OkHttpClient {
    return client.newBuilder()
      .connectTimeout(15, TimeUnit.MINUTES)
      .readTimeout(30, TimeUnit.MINUTES)
      .writeTimeout(30, TimeUnit.MINUTES)
      .build()
  }

  @Singleton
  @Provides
  @DefaultHttpClient
  fun provideDefaultOkHttpClient(@BaseHttpClient client: OkHttpClient): OkHttpClient {
    return client.newBuilder()
      .connectTimeout(45, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .writeTimeout(60, TimeUnit.SECONDS)
      .build()
  }

  @Singleton
  @Provides
  @ShortTimeoutHttpClient
  fun provideShortTimeoutOkHttpClient(@BaseHttpClient client: OkHttpClient): OkHttpClient {
    return client.newBuilder()
      .connectTimeout(10, TimeUnit.SECONDS)
      .readTimeout(20, TimeUnit.SECONDS)
      .writeTimeout(20, TimeUnit.SECONDS)
      .build()
  }
}