package com.asfoundation.wallet.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import androidx.work.WorkManager
import com.asfoundation.wallet.util.Log
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

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
  fun providesDelegatingWorkerFactory(): DelegatingWorkerFactory {
    return DelegatingWorkerFactory()
  }

}