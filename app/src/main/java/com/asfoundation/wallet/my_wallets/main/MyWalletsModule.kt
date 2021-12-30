package com.asfoundation.wallet.my_wallets.main

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
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
  fun providesNewMyWalletsNavigator(fragment: Fragment): MyWalletsNavigator {
    return MyWalletsNavigator(fragment, fragment.findNavController())
  }
}