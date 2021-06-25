package com.asfoundation.wallet.transactions

import com.asfoundation.wallet.MainActivityNavigator
import dagger.Module
import dagger.Provides

@Module
class PerkBonusAndGamificationServiceModule {

  @Provides
  fun provideHomeNavigator(service: PerkBonusAndGamificationService): MainActivityNavigator {
    return MainActivityNavigator(service)
  }
}