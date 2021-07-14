package com.asfoundation.wallet

import dagger.Module
import dagger.Provides

@Module
class MainActivityModule {

  @Provides
  fun provideNavigator(activity: MainActivity): MainActivityNavigator {
    return MainActivityNavigator(activity)
  }
}