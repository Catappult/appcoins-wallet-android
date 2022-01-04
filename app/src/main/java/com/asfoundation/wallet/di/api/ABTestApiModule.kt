package com.asfoundation.wallet.di.api

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.abtesting.ABTestApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ABTestApiModule {

  private val abTestUrl = BuildConfig.APTOIDE_WEB_SERVICES_AB_TEST_HOST

  @Singleton
  @Provides
  @Named("abtest-low-timer")
  fun provideABTestDefaultRetrofit(@Named("low-timer") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(abTestUrl)
        .build()
  }

  @Singleton
  @Provides
  fun providesWalletFeedbackApi(
      @Named("abtest-low-timer") retrofit: Retrofit): ABTestApi {
    return retrofit.create(ABTestApi::class.java)
  }
}