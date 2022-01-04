package com.asfoundation.wallet.di.api

import com.asfoundation.wallet.AirdropService
import com.asfoundation.wallet.base.RxSchedulers
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AirdropApiModule {

  private val airdropUrl = "https://api.appstorefoundation.org/"

  @Singleton
  @Provides
  @Named("airdrop-blockchain")
  fun provideAirdropDefaultRetrofit(@Named("blockchain") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(airdropUrl)
        .build()
  }

  @Provides
  fun provideAirdropApi(@Named("airdrop-blockchain") retrofit: Retrofit): AirdropService.Api {
    return retrofit.create(AirdropService.Api::class.java)
  }

  @Provides
  fun provideAirdropService(airdropApi: AirdropService.Api, gson: Gson,
                            rxSchedulers: RxSchedulers): AirdropService {
    return AirdropService(airdropApi, gson, rxSchedulers.io)
  }
}