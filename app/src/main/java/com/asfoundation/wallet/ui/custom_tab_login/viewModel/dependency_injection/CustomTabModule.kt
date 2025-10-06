package com.asfoundation.wallet.ui.custom_tab_login.viewModel.dependency_injection

import com.asfoundation.wallet.ui.custom_tab_login.viewModel.states.CustomTabVMStates
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal object CustomTabModule {
  @Provides
  fun provideCustomTabVMStates(): CustomTabVMStates {
    return CustomTabVMStates.Initial
  }
}