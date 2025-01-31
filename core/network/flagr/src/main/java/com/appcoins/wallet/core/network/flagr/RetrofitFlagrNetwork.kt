package com.appcoins.wallet.core.network.flagr

import com.appcoins.wallet.core.network.base.annotations.DefaultHttpClient
import com.appcoins.wallet.core.network.flagr.api.FlagrNetworkApi
import com.appcoins.wallet.core.network.flagr.model.FlagrRequest
import com.appcoins.wallet.core.network.flagr.model.FlagrResponse
import com.appcoins.wallet.core.utils.properties.HostProperties
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RetrofitFlagrNetwork {

  private val flagrUrl = HostProperties.FLAGR_BASE_HOST

  @Singleton
  @Provides
  @Named("flagr-default")
  fun provideFlagrDefaultRetrofit(@DefaultHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(flagrUrl)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  fun providesFlagrApi(
    @Named("flagr-default") retrofit: Retrofit
  ): FlagrNetworkApi {
    return retrofit.create(FlagrNetworkApi::class.java)
  }

}
