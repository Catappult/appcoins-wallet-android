package com.asfoundation.wallet.my_wallets.more

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import com.asfoundation.wallet.ui.wallets.WalletsInteract
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class MoreDialogModule {

  @Provides
  fun provideMoreDialogViewModelFactory(
    data: MoreDialogData,
    walletsInteract: WalletsInteract,
    observeWalletInfoUseCase: ObserveWalletInfoUseCase,
    observeDefaultWalletUseCase: ObserveDefaultWalletUseCase
  ): MoreDialogViewModelFactory =
    MoreDialogViewModelFactory(
      data,
      walletsInteract,
      observeWalletInfoUseCase,
      observeDefaultWalletUseCase
    )

  @Provides
  fun provideMoreDialogData(fragment: Fragment): MoreDialogData = fragment
    .requireArguments()
    .run {
      MoreDialogData(
        getString(MoreDialogFragment.WALLET_ADDRESS_KEY)!!,
        getString(MoreDialogFragment.FIAT_BALANCE_KEY)!!,
        getString(MoreDialogFragment.APPC_BALANCE_KEY)!!,
        getString(MoreDialogFragment.CREDITS_BALANCE_KEY)!!,
        getString(MoreDialogFragment.ETHEREUM_BALANCE_KEY)!!,
      )
    }

  @Provides
  fun provideMoreDialogNavigator(fragment: Fragment): MoreDialogNavigator =
    MoreDialogNavigator(fragment.findNavController())
}