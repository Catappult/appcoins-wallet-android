package com.asfoundation.wallet.my_wallets.change

import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor
import dagger.Module
import dagger.Provides

@Module
class ChangeActiveWalletDialogModule {

  @Provides
  fun provideChangeActiveWalletDialogViewModelFactory(data: ChangeActiveWalletDialogData,
                                                      walletDetailsInteractor: WalletDetailsInteractor)
      : ChangeActiveWalletDialogViewModelFactory {
    return ChangeActiveWalletDialogViewModelFactory(data, walletDetailsInteractor)
  }

  @Provides
  fun provideChangeActiveWalletDialogData(
      fragment: ChangeActiveWalletDialogFragment): ChangeActiveWalletDialogData {
    fragment.requireArguments()
        .apply {
          return ChangeActiveWalletDialogData(getSerializable(
              ChangeActiveWalletDialogFragment.WALLET_BALANCE_KEY)!! as WalletBalance)
        }
  }

  @Provides
  fun provideChangeActiveWalletDialogNavigator(
      fragment: ChangeActiveWalletDialogFragment): ChangeActiveWalletDialogNavigator {
    return ChangeActiveWalletDialogNavigator(fragment.findNavController())
  }
}