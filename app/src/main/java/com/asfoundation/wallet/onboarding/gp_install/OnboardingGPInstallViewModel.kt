package com.asfoundation.wallet.onboarding.gp_install

import androidx.lifecycle.viewModelScope
import com.asfoundation.wallet.app_start.AppStartUseCase
import com.asfoundation.wallet.app_start.StartMode
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OnboardingGPInstallSideEffect : SideEffect {
  data class LoadPackageNameIcon(val appPackageName: String) : OnboardingGPInstallSideEffect()
  data class NavigateBackToGame(val appPackageName: String) : OnboardingGPInstallSideEffect()
  object NavigateToExploreWallet : OnboardingGPInstallSideEffect()
}

object OnboardingGPInstallState : ViewState

@HiltViewModel
class OnboardingGPInstallViewModel @Inject constructor(
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val appStartUseCase: AppStartUseCase
) : BaseViewModel<OnboardingGPInstallState, OnboardingGPInstallSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingGPInstallState {
      return OnboardingGPInstallState
    }
  }

  fun handleLoadIcon() {
    viewModelScope.launch {
      val mode = appStartUseCase.startModes.first()
      sendSideEffect {
        OnboardingGPInstallSideEffect.LoadPackageNameIcon((mode as StartMode.PendingPurchaseFlow).packageName)
      }
    }
  }

  fun handleBackToGameClick() {
    viewModelScope.launch {
      val mode = appStartUseCase.startModes.first()
      sendSideEffect {
        OnboardingGPInstallSideEffect.NavigateBackToGame((mode as StartMode.PendingPurchaseFlow).packageName)
      }
    }
  }

  fun handleExploreWalletClick() {
    setOnboardingCompletedUseCase()
    sendSideEffect { OnboardingGPInstallSideEffect.NavigateToExploreWallet }
  }
}