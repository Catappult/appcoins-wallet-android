package com.asfoundation.wallet.onboarding.iap

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

sealed class OnboardingIapSideEffect : SideEffect {
  data class LoadPackageNameIcon(val appPackageName: String) : OnboardingIapSideEffect()
  data class NavigateBackToGame(val appPackageName: String) : OnboardingIapSideEffect()
  object NavigateToTermsConditions : OnboardingIapSideEffect()
}

object OnboardingIapState : ViewState

@HiltViewModel
class OnboardingIapViewModel @Inject constructor(
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val appStartUseCase: AppStartUseCase
) : BaseViewModel<OnboardingIapState, OnboardingIapSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingIapState {
      return OnboardingIapState
    }
  }

  fun handleLoadIcon() {
    viewModelScope.launch {
      val mode = appStartUseCase.startModes.first()
      sendSideEffect {
        OnboardingIapSideEffect.LoadPackageNameIcon((mode as StartMode.FirstUtm).packageName)
      }
    }
  }

  fun handleBackToGameClick() {
    viewModelScope.launch {
      val mode = appStartUseCase.startModes.first()
      sendSideEffect {
        OnboardingIapSideEffect.NavigateBackToGame((mode as StartMode.FirstUtm).packageName)
      }
    }
  }

  fun handleExploreWalletClick() {
    setOnboardingCompletedUseCase()
    sendSideEffect { OnboardingIapSideEffect.NavigateToTermsConditions }
  }
}