package com.asfoundation.wallet.firebase_messaging.di

import com.appcoins.wallet.core.network.microservices.annotations.BrokerDefaultRetrofit
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.RegisterFirebaseTokenUseCase
import com.asfoundation.wallet.firebase_messaging.domain.RegisterFirebaseTokenForWalletsUseCase
import com.asfoundation.wallet.firebase_messaging.domain.RegisterFirebaseTokenUseCaseImpl
import com.asfoundation.wallet.firebase_messaging.repository.FirebaseMessagingAPI
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface FirebaseMessagingModule {

  @Binds
  @Singleton
  fun provideRegisterFirebaseTokenForWalletsUseCase(registerFirebaseTokenUseCaseImpl: RegisterFirebaseTokenUseCaseImpl): RegisterFirebaseTokenForWalletsUseCase

  @Binds
  @Singleton
  fun provideRegisterFirebaseTokenUseCase(registerFirebaseTokenUseCase: RegisterFirebaseTokenUseCaseImpl): RegisterFirebaseTokenUseCase

  companion object {

    @Singleton
    @Provides
    fun provideFirebaseMessagingAPI(
      @BrokerDefaultRetrofit retrofit: Retrofit
    ): FirebaseMessagingAPI = retrofit.create(FirebaseMessagingAPI::class.java)

  }
}
