package com.asfoundation.wallet.my_wallets.token

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