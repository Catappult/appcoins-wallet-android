package com.asfoundation.wallet.di.api.microservices

import com.appcoins.wallet.core.utils.properties.HostProperties
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.share.BdsShareLinkRepository
import com.asfoundation.wallet.di.annotations.*
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
class DeeplinkApiModule {
  private val deeplinkUrl: String = "${HostProperties.MS_HOST}/deeplink/"

  @Singleton
  @Provides
  @DeeplinkDefaultRetrofit
  fun provideDeeplinkDefaultRetrofit(@DefaultHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(deeplinkUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  fun provideBdsShareLinkApi(
    @DeeplinkDefaultRetrofit retrofit: Retrofit
  ): BdsShareLinkRepository.BdsShareLinkApi {
    return retrofit.create(BdsShareLinkRepository.BdsShareLinkApi::class.java)
  }
}