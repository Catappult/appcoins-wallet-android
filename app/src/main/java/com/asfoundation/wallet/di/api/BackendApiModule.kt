package com.asfoundation.wallet.di.api

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.logging.send_logs.SendLogsRepository
import com.asfoundation.wallet.nfts.repository.NftApi
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import com.asfoundation.wallet.repository.IpCountryCodeProvider
import com.asfoundation.wallet.repository.OffChainTransactionsRepository
import com.asfoundation.wallet.ui.backup.success.BackupSuccessLogRepository
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BackendApiModule {

  private val backendUrl = BuildConfig.BACKEND_HOST

  @Singleton
  @Provides
  @Named("backend-blockchain")
  fun provideBackendBlockchainRetrofit(@Named("blockchain") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(backendUrl)
        .build()
  }

  @Singleton
  @Provides
  @Named("backend-default")
  fun provideBackendDefaultRetrofit(@Named("default") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(backendUrl)
        .build()
  }

  @Singleton
  @Provides
  @Named("backend-low-timer")
  fun provideBackendLowTimerRetrofit(@Named("low-timer") retrofit: Retrofit): Retrofit {
    return retrofit.newBuilder()
        .baseUrl(backendUrl)
        .build()
  }

  @Singleton
  @Provides
  fun providesIpCountryCodeApi(
      @Named("backend-default") retrofit: Retrofit): IpCountryCodeProvider.IpApi {
    return retrofit.create(IpCountryCodeProvider.IpApi::class.java)
  }

  @Singleton
  @Provides
  fun providesOffChainTransactionsApi(
      @Named("backend-blockchain")
      retrofit: Retrofit): OffChainTransactionsRepository.TransactionsApi {
    return retrofit.create(OffChainTransactionsRepository.TransactionsApi::class.java)
  }

  @Singleton
  @Provides
  fun providesPromoCodeBackendApi(
      @Named("backend-default") retrofit: Retrofit): PromoCodeRepository.PromoCodeBackendApi {
    return retrofit.create(PromoCodeRepository.PromoCodeBackendApi::class.java)
  }

  @Singleton
  @Provides
  fun providesSendLogsApi(
      @Named("backend-default") retrofit: Retrofit): SendLogsRepository.SendLogsApi {
    return retrofit.create(SendLogsRepository.SendLogsApi::class.java)
  }

  @Singleton
  @Provides
  fun providesNftApi(
      @Named("backend-default") retrofit: Retrofit): NftApi {
    return retrofit.create(NftApi::class.java)
  }

  @Singleton
  @Provides
  fun providesWalletInfoApi(
      @Named("backend-default") retrofit: Retrofit): WalletInfoRepository.WalletInfoApi {
    return retrofit.create(WalletInfoRepository.WalletInfoApi::class.java)
  }

  @Singleton
  @Provides
  fun providesBackupSuccessLogApi(
      @Named("backend-default") retrofit: Retrofit): BackupSuccessLogRepository.BackupLogApi {
    return retrofit.create(BackupSuccessLogRepository.BackupLogApi::class.java)
  }
}