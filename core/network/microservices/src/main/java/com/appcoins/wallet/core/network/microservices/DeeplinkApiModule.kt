package com.appcoins.wallet.core.network.microservices

import com.appcoins.wallet.core.network.base.annotations.DefaultHttpClient
import com.appcoins.wallet.core.network.microservices.annotations.DeeplinkDefaultRetrofit
import com.appcoins.wallet.core.network.microservices.api.deeplink.BdsShareLinkApi
import com.appcoins.wallet.core.utils.properties.HostProperties
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

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
  fun provideBdsShareLinkApi(@DeeplinkDefaultRetrofit retrofit: Retrofit): BdsShareLinkApi {
    return retrofit.create(BdsShareLinkApi::class.java)
  }
}
