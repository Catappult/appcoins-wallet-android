package com.asfoundation.wallet.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RxSchedulers {

  @NetworkDispatcher
  @Singleton
  @Provides
  fun provideNetworkDispatcher(): Scheduler =
    Schedulers.io()

  @ViewDispatcher
  @Singleton
  @Provides
  fun provideViewDispatcher(): Scheduler =
    AndroidSchedulers.mainThread()

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ViewDispatcher
