package com.asfoundation.wallet.onboarding.top_app

import androidx.lifecycle.viewModelScope
import com.asfoundation.wallet.app_start.AppStartUseCase
import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OnboardingGameSideEffect : SideEffect {
  data class LoadPackageNameIcon(val appPackageName: String) : OnboardingGameSideEffect()
  data class NavigateBackToGame(val appPackageName: String) : OnboardingGameSideEffect()
  object NavigateToTermsConditions : OnboardingGameSideEffect()
}

object OnboardingIapState : ViewState

@HiltViewModel
class OnboardingIapViewModel @Inject constructor(
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val appStartUseCase: AppStartUseCase
) : BaseViewModel<OnboardingIapState, OnboardingGameSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingIapState {
      return OnboardingIapState
    }
  }

  fun handleLoadIcon() {
    viewModelScope.launch {
      val mode = appStartUseCase.startModes.first()
      sendSideEffect {
        OnboardingGameSideEffect.LoadPackageNameIcon((mode as StartMode.FirstTopApp).packageName)
      }
    }
  }

  fun handleBackToGameClick() {
    viewModelScope.launch {
      val mode = appStartUseCase.startModes.first()
      sendSideEffect {
        OnboardingGameSideEffect.NavigateBackToGame((mode as StartMode.FirstTopApp).packageName)
      }
    }
  }

  fun handleExploreWalletClick() {
    setOnboardingCompletedUseCase()
    sendSideEffect { OnboardingGameSideEffect.NavigateToTermsConditions }
  }
}