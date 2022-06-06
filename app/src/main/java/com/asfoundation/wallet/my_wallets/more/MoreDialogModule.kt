package com.asfoundation.wallet.my_wallets.more

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class MoreDialogModule {

  @Provides
  fun provideMoreDialogViewModelFactory(data: MoreDialogData): MoreDialogViewModelFactory =
    MoreDialogViewModelFactory(data)

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
        getBoolean(MoreDialogFragment.SHOW_DELETE_WALLET_KEY),
      )
    }

  @Provides
  fun provideMoreDialogNavigator(fragment: Fragment): MoreDialogNavigator =
    MoreDialogNavigator(fragment.findNavController())
}