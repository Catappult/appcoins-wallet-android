package com.appcoins.wallet.core.utils.android_common

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DispatchersBindingsModule {

  @Binds
  @Singleton
  abstract fun bindDispatchers(impl: DispatchersImpl): Dispatchers

  @Binds
  abstract fun bindRxSchedulers(impl: RxSchedulersImpl): RxSchedulers
}