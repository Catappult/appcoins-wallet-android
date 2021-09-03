package com.asfoundation.wallet.di

import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.viewmodel.MyAddressViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class MyAddressModule {
  @Provides
  fun providesMyAddressViewModelFactory(transactionsRouter: TransactionsRouter) =
      MyAddressViewModelFactory(transactionsRouter)

  @Provides
  fun provideTransactionsRouter() = TransactionsRouter()
}