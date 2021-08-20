package com.asfoundation.wallet.my_wallets.token

import androidx.navigation.fragment.findNavController
import dagger.Module
import dagger.Provides

@Module
class TokenInfoDialogModule {

  @Provides
  fun provideTokenInfoDialogViewModelFactory(
      data: TokenInfoDialogData): TokenInfoDialogViewModelFactory {
    return TokenInfoDialogViewModelFactory(data)
  }

  @Provides
  fun provideTokenInfoDialogNavigator(fragment: TokenInfoDialogFragment): TokenInfoDialogNavigator {
    return TokenInfoDialogNavigator(fragment.findNavController())
  }

  @Provides
  fun provideTokenInfoDialogData(fragment: TokenInfoDialogFragment): TokenInfoDialogData {
    fragment.requireArguments()
        .apply {
          return TokenInfoDialogData(
              getString(TokenInfoDialogFragment.TITLE_KEY)!!,
              getString(TokenInfoDialogFragment.IMAGE_KEY)!!,
              getString(TokenInfoDialogFragment.DESCRIPTION_KEY)!!,
              getBoolean(TokenInfoDialogFragment.SHOW_TOP_UP_KEY, false),
          )
        }
  }
}