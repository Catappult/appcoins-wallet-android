package com.asfoundation.wallet.my_wallets

import com.asfoundation.wallet.main.MainActivityNavigator
import dagger.Module
import dagger.Provides

@Module
class MyWalletsModule {
  @Provides
  fun providesMyWalletsNavigator(fragment: MyWalletsFragment): MyWalletsNavigator {
    return MyWalletsNavigator(fragment, MainActivityNavigator(fragment.requireActivity()))
  }

  @Provides
  fun provideMainActivityNavigator(fragment: MyWalletsFragment): MainActivityNavigator {
    return MainActivityNavigator(fragment.requireActivity())
  }

}