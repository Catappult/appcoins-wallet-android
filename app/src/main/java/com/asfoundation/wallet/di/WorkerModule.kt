package com.asfoundation.wallet.di

import android.content.Context
import androidx.work.DelegatingWorkerFactory
import androidx.work.WorkManager
import com.asfoundation.wallet.promotions.worker.GetVipReferralWorkerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class WorkerModule {

  /**
   * This module serves as a base for injecting dependencies to workers
   * It initially was built to cancel a transaction but that was scrapped.
   *
   * To use it, simply create a factory for each worker and add it to the DelegatingWorkerFactory
   */
  @Singleton
  @Provides
  fun providesWorkManager(
    @ApplicationContext context: Context,
    getVipReferralWorkerFactory: GetVipReferralWorkerFactory
  ): WorkManager {
    return WorkManager.getInstance(context)
  }

  @Singleton
  @Provides
  fun providesDelegatingWorkerFactory(): DelegatingWorkerFactory {
    return DelegatingWorkerFactory()
  }

}