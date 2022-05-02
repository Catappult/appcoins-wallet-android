package com.asfoundation.wallet.onboarding

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.onboarding.use_cases.HasWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

sealed class OnboardingSideEffect : SideEffect {
  object NavigateToRecoverWallet : OnboardingSideEffect()
  object NavigateToLegalsConsent : OnboardingSideEffect()
  object NavigateToExit : OnboardingSideEffect()
}

data class OnboardingState(val pageNumber: Int = 0) : ViewState

@HiltViewModel
class OnboardingViewModel @Inject constructor(hasWalletUseCase: HasWalletUseCase) :
  BaseViewModel<OnboardingState, OnboardingSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingState {
      return OnboardingState()
    }
  }

  var rootPage = 0

  init {
    hasWalletUseCase()
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess {
        if (it) {
          rootPage = 1
          setState { copy(pageNumber = 1) }
        }
      }
      .scopedSubscribe { it.printStackTrace() }
  }

  fun handleBackButtonClick() = state.pageNumber.run {
    if (this == rootPage) {
      sendSideEffect { OnboardingSideEffect.NavigateToExit }
    } else {
      setState { copy(pageNumber = pageNumber - 1) }
    }
  }

  fun handleNextClick() {
    setState { copy(pageNumber = 1) }
  }

  fun handleGetStartedClick() {
    sendSideEffect { OnboardingSideEffect.NavigateToLegalsConsent }
  }

  fun handleRecoverClick() {
    sendSideEffect { OnboardingSideEffect.NavigateToRecoverWallet }
  }
}