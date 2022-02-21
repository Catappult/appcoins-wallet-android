package com.asfoundation.wallet.onboarding

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class OnboardingSideEffect : SideEffect {
  object NavigateToRecoverWallet : OnboardingSideEffect()
}

data class OnboardingState(val pageNumber: Int = 0) : ViewState

@HiltViewModel
class OnboardingViewModel @Inject constructor() :
  BaseViewModel<OnboardingState, OnboardingSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingState {
      return OnboardingState(0)
    }
  }

  fun handleBackButtonClick() {
    setState { copy(pageNumber = 0) }
  }

  fun handleNextClick() {
    setState { copy(pageNumber = 1) }
  }

  fun handleRecoverClick() {
    sendSideEffect { OnboardingSideEffect.NavigateToRecoverWallet }
  }
}