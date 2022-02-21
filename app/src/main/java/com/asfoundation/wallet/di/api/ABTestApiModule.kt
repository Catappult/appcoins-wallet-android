package com.asfoundation.wallet.di.api

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.abtesting.ABTestApi
import com.asfoundation.wallet.di.annotations.LowTimerHttpClient
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
class ABTestApiModule {

  private val abTestUrl = BuildConfig.APTOIDE_WEB_SERVICES_AB_TEST_HOST

  @Singleton
  @Provides
  @Named("abtest-low-timer")
  fun provideABTestDefaultRetrofit(@LowTimerHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(abTestUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  fun providesWalletFeedbackApi(
    @Named("abtest-low-timer") retrofit: Retrofit
  ): ABTestApi {
    return retrofit.create(ABTestApi::class.java)
  }
}