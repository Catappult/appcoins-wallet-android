package com.asfoundation.wallet.di

import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import androidx.work.WorkManager
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.asfoundation.wallet.billing.partners.AddressService
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.transactions.CancelTransactionWorkerFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class WorkerModule {

  @Singleton
  @Provides
  fun providesWorkManager(context: Context,
                          delegatingWorkerFactory: DelegatingWorkerFactory): WorkManager {
    val config = Configuration.Builder()
        .setWorkerFactory(delegatingWorkerFactory)
        .setMinimumLoggingLevel(Log.DEBUG)
        .build()

    WorkManager.initialize(context, config)

    return WorkManager.getInstance(context)
  }


  @Singleton
  @Provides
  fun providesDelegatingWorkerFactory(
      cancelTransactionWorkerFactory: CancelTransactionWorkerFactory): DelegatingWorkerFactory {
    return DelegatingWorkerFactory().apply {
      addFactory(cancelTransactionWorkerFactory)
    }
  }

  @Singleton
  @Provides
  fun providesTransactionWorkerFactory(remoteRepository: RemoteRepository,
                                       walletService: WalletService,
                                       partnerAddressService: AddressService,
                                       logger: Logger): CancelTransactionWorkerFactory {
    return CancelTransactionWorkerFactory(remoteRepository, walletService, partnerAddressService,
        logger)
  }
}