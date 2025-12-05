package com.asfoundation.wallet.currency_setup_cg.viewModel.states

internal sealed interface SetUpCurrencyVMState {
  object Idle : SetUpCurrencyVMState
  object Processing : SetUpCurrencyVMState
  object Success : SetUpCurrencyVMState
  object Error : SetUpCurrencyVMState
}