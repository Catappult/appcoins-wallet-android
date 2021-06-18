package com.asfoundation.wallet.di

import com.asfoundation.wallet.router.TransactionsRouter
import dagger.Module
import dagger.Provides

@Module
class MyAddressModule {

  @Provides
  fun provideTransactionsRouter() = TransactionsRouter()
}