package com.asfoundation.wallet.di.api

import com.asfoundation.wallet.analytics.AnalyticsAPI
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AnalyticsApiModule {

  private val analyticsUrl = "https://ws75.aptoide.com/api/7/"

  @Singleton
  @Provides
  @Named("analytics-default")
  fun provideAnalyticsDefaultRetrofit(@Named("default") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(analyticsUrl)
        .build()
  }

  @Singleton
  @Provides
  fun provideAnalyticsAPI(
      @Named("analytics-default") retrofit: Retrofit, objectMapper: ObjectMapper): AnalyticsAPI {
    return retrofit.newBuilder()
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build()
        .create(AnalyticsAPI::class.java)
  }
}