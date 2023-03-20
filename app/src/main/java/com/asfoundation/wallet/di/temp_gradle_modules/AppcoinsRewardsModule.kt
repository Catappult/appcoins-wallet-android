package com.asfoundation.wallet.di.temp_gradle_modules

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewards
import com.appcoins.wallet.appcoins.rewards.ErrorMapper
import com.appcoins.wallet.appcoins.rewards.repository.BdsAppcoinsRewardsRepository
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.core.utils.jvm_common.MemoryCache
import com.asfoundation.wallet.billing.CreditsRemoteRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppcoinsRewardsModule {
  @Singleton
  @Provides
  fun provideAppcoinsRewards(walletService: WalletService, billing: Billing,
                             remoteRepository: RemoteRepository,
                             errorMapper: ErrorMapper): AppcoinsRewards {
    return AppcoinsRewards(
        BdsAppcoinsRewardsRepository(CreditsRemoteRepository(remoteRepository)),
        object : com.appcoins.wallet.appcoins.rewards.repository.WalletService {
          override fun getWalletAddress() = walletService.getWalletAddress()

          override fun signContent(content: String) = walletService.signContent(content)
        },
      MemoryCache(
        BehaviorSubject.create(),
        ConcurrentHashMap()
      ), Schedulers.io(), billing,
        errorMapper)
  }

  @Singleton
  @Provides
  fun providesErrorMapper(gson: Gson): ErrorMapper {
    return ErrorMapper(gson)
  }
}