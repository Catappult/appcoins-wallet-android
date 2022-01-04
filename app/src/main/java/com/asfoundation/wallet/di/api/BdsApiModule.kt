package com.asfoundation.wallet.di.api

import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.asf.wallet.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BdsApiModule {

  private val bdsUrl = BuildConfig.BDS_BASE_HOST

  @Singleton
  @Provides
  @Named("bds-default")
  fun provideAnalyticsDefaultRetrofit(@Named("default") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(bdsUrl)
        .build()
  }

  @Singleton
  @Provides
  fun provideBdsApiSecondary(
      @Named("bds-default") retrofit: Retrofit): BdsApiSecondary {
    return retrofit.create(BdsApiSecondary::class.java)
  }
}