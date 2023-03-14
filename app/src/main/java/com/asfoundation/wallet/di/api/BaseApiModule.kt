package com.asfoundation.wallet.di.api

import android.content.Context
import com.asfoundation.wallet.billing.paypal.repository.MagnesHeaderInterceptor
import com.asfoundation.wallet.di.annotations.BaseHttpClient
import com.asfoundation.wallet.di.annotations.BlockchainHttpClient
import com.asfoundation.wallet.di.annotations.DefaultHttpClient
import com.asfoundation.wallet.di.annotations.ShortTimeoutHttpClient
import com.asfoundation.wallet.util.LogInterceptor
import com.asfoundation.wallet.util.UserAgentInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
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