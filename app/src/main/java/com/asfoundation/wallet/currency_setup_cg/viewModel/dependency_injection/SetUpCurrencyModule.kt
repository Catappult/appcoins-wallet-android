package com.asfoundation.wallet.currency_setup_cg.viewModel.dependency_injection

import com.asfoundation.wallet.currency_setup_cg.viewModel.states.SetUpCurrencyVMState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
internal object SetUpCurrencyModule {
  @Provides
  fun providesSetUpCurrencyVMStates(): SetUpCurrencyVMState {
    return SetUpCurrencyVMState.Idle
  }
}