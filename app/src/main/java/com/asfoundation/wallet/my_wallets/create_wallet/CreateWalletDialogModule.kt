package com.asfoundation.wallet.my_wallets.create_wallet

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class CreateWalletDialogModule {

  @Provides
  fun provideCreateWalletDialogViewModelFactory(
      walletsInteract: WalletsInteract): CreateWalletDialogViewModelFactory {
    return CreateWalletDialogViewModelFactory(walletsInteract)
  }

  @Provides
  fun provideCreateWalletDialogNavigator(fragment: Fragment): CreateWalletDialogNavigator {
    return CreateWalletDialogNavigator(fragment.findNavController())
  }
}