package com.asfoundation.wallet.onboarding

import androidx.lifecycle.SavedStateHandle
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.onboarding.OnboardingFragment.Companion.ONBOARDING_FROM_IAP
import com.asfoundation.wallet.onboarding.use_cases.HasWalletUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class OnboardingSideEffect : SideEffect {
  object NavigateToWalletCreationAnimation : OnboardingSideEffect()
  object NavigateToRecoverWallet : OnboardingSideEffect()
  object NavigateToLegalsConsent : OnboardingSideEffect()
  object NavigateToExit : OnboardingSideEffect()
}

data class OnboardingState(val pageNumber: Int = 0) : ViewState

@HiltViewModel
class OnboardingViewModel @Inject constructor(
  private val hasWalletUseCase: HasWalletUseCase,
  private val rxSchedulers: RxSchedulers,
  private val savedStateHandle: SavedStateHandle
) :
  BaseViewModel<OnboardingState, OnboardingSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingState {
      return OnboardingState()
    }
  }

  var rootPage = 0

  init {
    val isFromIap = savedStateHandle.get<Boolean>(ONBOARDING_FROM_IAP)!!
    rootPage = when (isFromIap) {
      true -> 0
      false -> 1
    }
    checkWallets(isFromIap)
  }

  private fun checkWallets(isFromIap: Boolean) {
    hasWalletUseCase()
      .observeOn(rxSchedulers.main)
      .doOnSuccess {
        if (!it) {
          if (isFromIap) {
            sendSideEffect {
              OnboardingSideEffect.NavigateToWalletCreationAnimation
            }
          } else {
            rootPage = 1
            setState { copy(pageNumber = 1) }
          }
        } else {
          rootPage = 2
          setState { copy(pageNumber = 2) }
        }
      }
      .scopedSubscribe { it.printStackTrace() }
  }

  fun handleBackButtonClick() = state.pageNumber.run {
    if (this == rootPage) {
      sendSideEffect { OnboardingSideEffect.NavigateToExit }
    } else {
      setState { copy(pageNumber = pageNumber - 2) }
    }
  }

  fun handleNextClick() {
    setState { copy(pageNumber = 2) }
  }

  fun handleGetStartedClick() {
    sendSideEffect { OnboardingSideEffect.NavigateToLegalsConsent }
  }

  fun handleRecoverClick() {
    sendSideEffect { OnboardingSideEffect.NavigateToRecoverWallet }
  }
}