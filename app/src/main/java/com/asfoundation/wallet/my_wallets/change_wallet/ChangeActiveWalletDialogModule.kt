package com.asfoundation.wallet.my_wallets.change_wallet

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.ui.wallets.WalletDetailsInteractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class ChangeActiveWalletDialogModule {

  @Provides
  fun provideChangeActiveWalletDialogViewModelFactory(
      data: ChangeActiveWalletDialogData,
      walletDetailsInteractor: WalletDetailsInteractor): ChangeActiveWalletDialogViewModelFactory {
    return ChangeActiveWalletDialogViewModelFactory(data, walletDetailsInteractor)
  }

  @Provides
  fun provideChangeActiveWalletDialogData(fragment: Fragment): ChangeActiveWalletDialogData {
    fragment.requireArguments()
        .apply {
          return ChangeActiveWalletDialogData(getSerializable(
              ChangeActiveWalletDialogFragment.WALLET_BALANCE_KEY)!! as WalletBalance)
        }
  }
}