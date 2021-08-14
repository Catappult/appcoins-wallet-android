package com.asfoundation.wallet.my_wallets.neww

import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import dagger.Module
import dagger.Provides

@Module
class NewMyWalletsModule {

  @Provides
  fun providesMyWalletsViewModelFactory(balanceInteractor: BalanceInteractor,
                                        walletsInteract: WalletsInteract): MyWalletsViewModelFactory {
    return MyWalletsViewModelFactory(balanceInteractor, walletsInteract)
  }

  @Provides
  fun providesNewMyWalletsNavigator(fragment: NewMyWalletsFragment): NewMyWalletsNavigator {
    return NewMyWalletsNavigator(fragment.findNavController())
  }
}