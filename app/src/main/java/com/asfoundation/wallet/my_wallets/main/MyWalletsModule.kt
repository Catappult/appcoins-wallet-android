package com.asfoundation.wallet.my_wallets.main

import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import dagger.Module
import dagger.Provides

@Module
class MyWalletsModule {

  @Provides
  fun providesMyWalletsViewModelFactory(balanceInteractor: BalanceInteractor,
                                        walletsInteract: WalletsInteract,
                                        observeDefaultWalletUseCase: ObserveDefaultWalletUseCase): MyWalletsViewModelFactory {
    return MyWalletsViewModelFactory(balanceInteractor, walletsInteract,
        observeDefaultWalletUseCase)
  }

  @Provides
  fun providesNewMyWalletsNavigator(fragment: MyWalletsFragment): MyWalletsNavigator {
    return MyWalletsNavigator(fragment, fragment.findNavController())
  }
}