package com.asfoundation.wallet.my_wallets.neww

import com.asfoundation.wallet.ui.balance.BalanceInteractor
import dagger.Module
import dagger.Provides

@Module
class NewMyWalletsModule {

  @Provides
  fun providesMyWalletsViewModelFactory(
      balanceInteractor: BalanceInteractor): MyWalletsViewModelFactory {
    return MyWalletsViewModelFactory(balanceInteractor)
  }
}