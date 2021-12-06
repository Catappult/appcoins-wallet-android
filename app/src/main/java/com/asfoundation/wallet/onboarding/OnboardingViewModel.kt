package com.asfoundation.wallet.onboarding

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState

sealed class OnboardingSideEffect : SideEffect {
  object NavigateToValuePropositions : OnboardingSideEffect()
  object NavigateToRecoverWallet : OnboardingSideEffect()
  object NavigateBackToWelcomeScreen : OnboardingSideEffect()
}

data class OnboardinState(val pageNumber: Int) : ViewState

class OnboardingViewModel : BaseViewModel<OnboardinState, OnboardingSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardinState {
      return OnboardinState(0)
    }
  }

  fun handleBackButtonClick() {
    sendSideEffect { OnboardingSideEffect.NavigateBackToWelcomeScreen }
    setState { copy(pageNumber = 0) }
  }

  fun handleNextClick() {
    sendSideEffect { OnboardingSideEffect.NavigateToValuePropositions }
    setState { copy(pageNumber = 1) }
  }

  fun handleRecoverClick() {
    sendSideEffect { OnboardingSideEffect.NavigateToRecoverWallet }
  }
}