package com.asfoundation.wallet.onboarding.iap

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.onboarding.use_cases.GetOnboardingFromIapPackageNameUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingFromIapStateUseCase
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.onboarding.use_cases.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class OnboardingIapSideEffect : SideEffect {
  data class LoadPackageNameIcon(val appPackageName: String?) : OnboardingIapSideEffect()
  object NavigateToWalletCreationAnimation : OnboardingIapSideEffect()
  object NavigateBackToGame : OnboardingIapSideEffect()
  object NavigateToTermsConditions : OnboardingIapSideEffect()
}

object OnboardingIapState : ViewState

@HiltViewModel
class OnboardingIapViewModel @Inject constructor(
  private val hasWalletUseCase: HasWalletUseCase,
  private val rxSchedulers: RxSchedulers,
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val setOnboardingFromIapStateUseCase: SetOnboardingFromIapStateUseCase,
  private val getOnboardingFromIapPackageNameUseCase: GetOnboardingFromIapPackageNameUseCase
) : BaseViewModel<OnboardingIapState, OnboardingIapSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingIapState {
      return OnboardingIapState
    }
  }

  init {
    setOnboardingFromIapStateUseCase(state = false)
  }

  fun handleLoadIcon() {
    sendSideEffect {
      OnboardingIapSideEffect.LoadPackageNameIcon(getOnboardingFromIapPackageNameUseCase())
    }
  }

  fun handleCreateWallet() {
    hasWalletUseCase()
      .observeOn(rxSchedulers.main)
      .doOnSuccess {
        if (!it) {
          setOnboardingFromIapStateUseCase(state = false)
          sendSideEffect {
            OnboardingIapSideEffect.LoadPackageNameIcon(getOnboardingFromIapPackageNameUseCase())
          }
          sendSideEffect {
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