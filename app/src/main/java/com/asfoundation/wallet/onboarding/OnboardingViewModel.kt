package com.asfoundation.wallet.onboarding

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.onboarding.use_cases.HasWalletUseCase
import com.asfoundation.wallet.onboarding.use_cases.IsOnboardingFromIapUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class OnboardingSideEffect : SideEffect {
  object NavigateToWalletCreationAnimation : OnboardingSideEffect()
  object NavigateToRecoverWallet : OnboardingSideEffect()
  object NavigateToLegalsConsent : OnboardingSideEffect()
  object NavigateToExit : OnboardingSideEffect()
}

data class OnboardingState(val pageContent: OnboardingContent = OnboardingContent.EMPTY) : ViewState

@HiltViewModel
class OnboardingViewModel @Inject constructor(
  private val hasWalletUseCase: HasWalletUseCase,
  private val rxSchedulers: RxSchedulers,
  private val isOnboardingFromIapUseCase: IsOnboardingFromIapUseCase
) :
  BaseViewModel<OnboardingState, OnboardingSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingState {
      return OnboardingState()
    }
  }

  var rootPage = OnboardingContent.EMPTY

  init {
    val fromIap = isOnboardingFromIapUseCase()
    rootPage = when (fromIap) {
      true -> OnboardingContent.EMPTY
      false -> OnboardingContent.WELCOME
    }
    checkWallets(fromIap)
  }

  private fun checkWallets(fromIap: Boolean) {
    hasWalletUseCase()
      .observeOn(rxSchedulers.main)
      .doOnSuccess {
        if (!it) {
          rootPage = OnboardingContent.WELCOME
          if (fromIap) {
            sendSideEffect {
              OnboardingSideEffect.NavigateToWalletCreationAnimation
            }
          } else {
            setState { copy(pageContent = OnboardingContent.WELCOME) }
          }
        } else {
          rootPage = OnboardingContent.VALUES
          setState { copy(pageContent = OnboardingContent.VALUES) }
        }
      }
      .scopedSubscribe { it.printStackTrace() }
  }

  fun handleBackButtonClick() = state.pageContent.run {
    if (this == rootPage) {
      sendSideEffect { OnboardingSideEffect.NavigateToExit }
    } else {
      setState { copy(pageContent = OnboardingContent.WELCOME) }
    }
  }

  fun handleNextClick() {
    setState { copy(pageContent = OnboardingContent.VALUES) }
  }

  fun handleGetStartedClick() {
    sendSideEffect { OnboardingSideEffect.NavigateToLegalsConsent }
  }

  fun handleRecoverClick() {
    sendSideEffect { OnboardingSideEffect.NavigateToRecoverWallet }
  }
}