package com.asfoundation.wallet.ui.backup.save

import com.asfoundation.wallet.ui.backup.skip.SkipDialogFragment
import com.asfoundation.wallet.ui.backup.skip.SkipDialogNavigator
import com.asfoundation.wallet.ui.backup.skip.SkipDialogViewModelFactory
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
    return SkipDialogNavigator(fragment, fragment.requireFragmentManager())
  }
}