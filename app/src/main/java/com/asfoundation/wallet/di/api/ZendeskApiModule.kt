package com.asfoundation.wallet.di.api

import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.networkbase.annotations.DefaultHttpClient
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.rating.RatingRepository
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
class ZendeskApiModule {

  private val zendeskUrl = HostProperties.FEEDBACK_ZENDESK_BASE_HOST

  @Singleton
  @Provides
  @Named("zendesk-default")
  fun provideZendeskDefaultRetrofit(@DefaultHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(zendeskUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  fun providesWalletFeedbackApi(
    @Named("zendesk-default") retrofit: Retrofit
  ): RatingRepository.WalletFeedbackApi {
    return retrofit.create(RatingRepository.WalletFeedbackApi::class.java)
  }
}