package com.appcoins.wallet.core.network.zendesk

import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.core.network.base.annotations.DefaultHttpClient
import com.appcoins.wallet.core.network.zendesk.model.WalletFeedbackBody
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RetrofitZendeskNetwork {

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
  ): RetrofitZendeskNetworkApi {
    return retrofit.create(RetrofitZendeskNetworkApi::class.java)
  }

  interface RetrofitZendeskNetworkApi {
    @POST("tickets.json")
    fun sendFeedback(
      @Header("Authorization") authorization: String,
      @Body feedback: WalletFeedbackBody
    ): Single<Response<ResponseBody>>
  }
}