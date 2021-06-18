package com.asfoundation.wallet.advertise

import com.asfoundation.wallet.main.MainActivityNavigator
import dagger.Module
import dagger.Provides

@Module
class WalletPoAServiceModule {
  @Provides
  fun provideHomeNavigator(service: WalletPoAService): MainActivityNavigator {
    return MainActivityNavigator(service)
  }
}