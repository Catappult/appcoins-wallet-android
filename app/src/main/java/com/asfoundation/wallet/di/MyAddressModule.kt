package com.asfoundation.wallet.di

import com.asfoundation.wallet.router.TransactionsRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module
class MyAddressModule {

  @Provides
  fun provideTransactionsRouter() = TransactionsRouter()
}