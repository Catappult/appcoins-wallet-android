package com.asfoundation.wallet.ui.backup.skip

import dagger.Module
import dagger.Provides

@Module
class SkipDialogModule {

  @Provides
  fun providesSkipDialogViewModelFactory(): SkipDialogViewModelFactory {
    return SkipDialogViewModelFactory()
  }

  @Provides
  fun providesSkipDialogNavigator(
      fragment: SkipDialogFragment
  ): SkipDialogNavigator {
    return SkipDialogNavigator(fragment)
  }
}
