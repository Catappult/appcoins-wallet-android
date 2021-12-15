package com.asfoundation.wallet.my_wallets.main

import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import dagger.Module
import dagger.Provides

@Module
class MyWalletsModule {

  @Provides
  fun providesMyWalletsViewModelFactory(balanceInteractor: BalanceInteractor,
                                        walletsInteract: WalletsInteract,
                                        observeWalletInfoUseCase: ObserveWalletInfoUseCase,
                                        observeDefaultWalletUseCase: ObserveDefaultWalletUseCase): MyWalletsViewModelFactory {
    return MyWalletsViewModelFactory(balanceInteractor, walletsInteract,
        observeWalletInfoUseCase, observeDefaultWalletUseCase)
  }

  @Provides
  fun providesNewMyWalletsNavigator(fragment: MyWalletsFragment): MyWalletsNavigator {
    return MyWalletsNavigator(fragment, fragment.findNavController())
  }
}