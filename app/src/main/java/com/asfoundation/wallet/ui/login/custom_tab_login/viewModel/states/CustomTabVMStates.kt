package com.asfoundation.wallet.ui.login.custom_tab_login.viewModel.states

import com.asfoundation.wallet.ui.login.usecases.FetchUserKeyUseCase.FetchUserKeyResult

internal sealed class CustomTabVMStates {
  object Initial : CustomTabVMStates()
  object FetchingUserKey : CustomTabVMStates()
  data class FinishActivity(
    val response: FetchUserKeyResult
  ) : CustomTabVMStates()
  object FinishWithError : CustomTabVMStates()
}