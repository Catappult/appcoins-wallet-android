package com.asfoundation.wallet.my_wallets.verify_picker

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class VerifyPickerDialogModule {

  @Provides
  fun provideVerifyPickerDialogNavigator(fragment: Fragment): VerifyPickerDialogNavigator {
    return VerifyPickerDialogNavigator(fragment.findNavController())
  }
}