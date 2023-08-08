package com.appcoins.wallet.core.network.eskills

import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.core.network.base.annotations.DefaultHttpClient
import com.appcoins.wallet.core.network.eskills.annotations.EskillsCarouselRetrofit
import com.appcoins.wallet.core.network.eskills.api.EskillsGamesApi
import com.appcoins.wallet.core.network.eskills.api.RoomApi
import com.appcoins.wallet.core.network.eskills.api.TicketApi
import com.google.gson.GsonBuilder
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
class EskillsApiModule {

  private val eskillsUrl = HostProperties.SKILLS_HOST

  private val carouselUrl = HostProperties.ESKILLS_CAROUSEL_HOST

  @Singleton
  @Provides
  @Named("eskills-default")
  fun provideAnalyticsDefaultRetrofit(@DefaultHttpClient client: OkHttpClient): Retrofit {
    val gson = GsonBuilder()
      .setDateFormat("yyyy-MM-dd HH:mm")
      .create()
    return Retrofit.Builder()
      .baseUrl(eskillsUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  @EskillsCarouselRetrofit
  fun provideEskillsCarouselRetrofit(@DefaultHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(carouselUrl)
      .client(client)
      .addConverterFactory(
        GsonConverterFactory.create(
          GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create()
        )
      )
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  fun provideBdsApiSecondary(
    @Named("eskills-default") retrofit: Retrofit
  ): RoomApi {
    return retrofit.create(RoomApi::class.java)
  }

  @Provides
  fun providesTicketsRepository(
    @Named("eskills-default") retrofit: Retrofit
  ): TicketApi {
    return retrofit.create(TicketApi::class.java)
  }

  @Singleton
  @Provides
  fun providesEskillsGamesApi(
    @EskillsCarouselRetrofit retrofit: Retrofit
  ): EskillsGamesApi {
    return retrofit.create(EskillsGamesApi::class.java)
  }
}