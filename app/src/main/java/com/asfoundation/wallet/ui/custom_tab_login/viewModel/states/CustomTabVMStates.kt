package com.asfoundation.wallet.ui.custom_tab_login.viewModel.states

internal sealed class CustomTabVMStates {
  object Initial : CustomTabVMStates()
  object FetchingUserKey : CustomTabVMStates()
  object FinishActivity : CustomTabVMStates()
  object FinishWithError : CustomTabVMStates()
}