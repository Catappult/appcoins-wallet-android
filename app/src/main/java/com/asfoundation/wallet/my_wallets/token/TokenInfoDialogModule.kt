package com.asfoundation.wallet.my_wallets.token

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class TokenInfoDialogModule {

  @Provides
  fun provideTokenInfoDialogViewModelFactory(
      data: TokenInfoDialogData): TokenInfoDialogViewModelFactory {
    return TokenInfoDialogViewModelFactory(data)
  }

  @Provides
  fun provideTokenInfoDialogNavigator(fragment: Fragment): TokenInfoDialogNavigator {
    return TokenInfoDialogNavigator(fragment.findNavController())
  }

  @Provides
  fun provideTokenInfoDialogData(fragment: Fragment): TokenInfoDialogData {
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