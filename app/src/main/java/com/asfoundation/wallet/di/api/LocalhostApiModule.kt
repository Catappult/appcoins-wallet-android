package com.asfoundation.wallet.di.api

import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class LocalhostApiModule {

  private val localhost = "https://localhost/"

  @Singleton
  @Provides
  @Named("localhost-default")
  fun provideLocalhostDefaultRetrofit(@Named("default") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(localhost)
        .build()
  }

  @Singleton
  @Provides
  fun providesAwsUploadFilesApi(
      @Named("default") retrofit: Retrofit): SendLogsRepository.AwsUploadFilesApi {
    return retrofit.create(SendLogsRepository.AwsUploadFilesApi::class.java)
  }
}