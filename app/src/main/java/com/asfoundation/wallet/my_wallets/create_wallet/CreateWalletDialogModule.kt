package com.asfoundation.wallet.my_wallets.create_wallet

import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import dagger.Module
import dagger.Provides

@Module
class CreateWalletDialogModule {

  @Provides
  fun provideCreateWalletDialogViewModelFactory(
      walletsInteract: WalletsInteract): CreateWalletDialogViewModelFactory {
    return CreateWalletDialogViewModelFactory(walletsInteract)
  }

  @Provides
  fun provideCreateWalletDialogNavigator(
      fragment: CreateWalletDialogFragment): CreateWalletDialogNavigator {
    return CreateWalletDialogNavigator(fragment.findNavController())
  }
}