package com.asfoundation.wallet.onboarding.iap

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.onboarding.use_cases.HasWalletUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingFromIapUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class OnboardingIapSideEffect : SideEffect {
  object NavigateToWalletCreationAnimation : OnboardingIapSideEffect()
  object NavigateBackToGame : OnboardingIapSideEffect()
  object NavigateToTermsConditions : OnboardingIapSideEffect()
  object ShowContent : OnboardingIapSideEffect()
}

object OnboardingIapState : ViewState

@HiltViewModel
class OnboardingIapViewModel @Inject constructor(
  private val hasWalletUseCase: HasWalletUseCase,
  private val rxSchedulers: RxSchedulers,
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val setOnboardingFromIapUseCase: SetOnboardingFromIapUseCase
) : BaseViewModel<OnboardingIapState, OnboardingIapSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingIapState {
      return OnboardingIapState
    }
  }

  fun handleCreateWallet() {
    hasWalletUseCase()
      .observeOn(rxSchedulers.main)
      .doOnSuccess {
        sendSideEffect {
          setOnboardingFromIapUseCase(state = false)
          if (it) {
            OnboardingIapSideEffect.ShowContent
          } else {
            OnboardingIapSideEffect.NavigateToWalletCreationAnimation
          }
        }
      }
      .scopedSubscribe { it.printStackTrace() }
  }

  fun handleBackToGameClick() {
    sendSideEffect { OnboardingIapSideEffect.NavigateBackToGame }
  }

  fun handleExploreWalletClick() {
    setOnboardingCompletedUseCase()
    sendSideEffect { OnboardingIapSideEffect.NavigateToTermsConditions }
  }
}