package com.asfoundation.wallet.my_wallets.verify_picker

import androidx.navigation.fragment.findNavController
import dagger.Module
import dagger.Provides

@Module
class VerifyPickerDialogModule {

  @Provides
  fun provideVerifyPickerDialogNavigator(
      fragment: VerifyPickerDialogFragment): VerifyPickerDialogNavigator {
    return VerifyPickerDialogNavigator(fragment.findNavController())
  }
}