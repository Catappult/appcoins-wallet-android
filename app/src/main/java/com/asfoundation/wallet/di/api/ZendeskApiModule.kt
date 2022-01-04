package com.asfoundation.wallet.di.api

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.rating.RatingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ZendeskApiModule {

  private val zendeskUrl = BuildConfig.FEEDBACK_ZENDESK_BASE_HOST

  @Singleton
  @Provides
  @Named("zendesk-default")
  fun provideZendeskDefaultRetrofit(@Named("default") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(zendeskUrl)
        .build()
  }

  @Singleton
  @Provides
  fun providesWalletFeedbackApi(
      @Named("zendesk-default") retrofit: Retrofit): RatingRepository.WalletFeedbackApi {
    return retrofit.create(RatingRepository.WalletFeedbackApi::class.java)
  }
}