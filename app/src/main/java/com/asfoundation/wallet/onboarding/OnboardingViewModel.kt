package com.asfoundation.wallet.onboarding

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.asfoundation.wallet.app_start.AppStartUseCase
import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.onboarding.use_cases.HasWalletUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OnboardingSideEffect : SideEffect {
  data class NavigateToLink(val uri: Uri) : OnboardingSideEffect()
  object NavigateToWalletCreationAnimation : OnboardingSideEffect()
  object NavigateToRecoverWallet : OnboardingSideEffect()
  object NavigateToFinish : OnboardingSideEffect()
}

data class OnboardingState(val pageContent: OnboardingContent = OnboardingContent.EMPTY) : ViewState

@HiltViewModel
class OnboardingViewModel @Inject constructor(
  private val hasWalletUseCase: HasWalletUseCase,
  private val rxSchedulers: RxSchedulers,
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  appStartUseCase: AppStartUseCase
) :
  BaseViewModel<OnboardingState, OnboardingSideEffect>(initialState()) {

  companion object {
    fun initialState(): OnboardingState {
      return OnboardingState()
    }
  }

  init {
    handleLaunchMode(appStartUseCase)
  }

  fun handleLaunchMode(appStartUseCase: AppStartUseCase) {
    viewModelScope.launch {
      val mode = appStartUseCase.startModes.first()
      when (mode) {
        is StartMode.PendingPurchaseFlow -> sendSideEffect { OnboardingSideEffect.NavigateToWalletCreationAnimation }
        is StartMode.GPInstall -> sendSideEffect { OnboardingSideEffect.NavigateToWalletCreationAnimation }
        else -> setState { copy(pageContent = OnboardingContent.VALUES) }
      }
    }
  }

  fun handleLaunchWalletClick() {
    hasWalletUseCase()
      .observeOn(rxSchedulers.main)
      .doOnSuccess {
        setOnboardingCompletedUseCase()
        sendSideEffect {
          if (it) {
            OnboardingSideEffect.NavigateToFinish
          } else {
            OnboardingSideEffect.NavigateToWalletCreationAnimation
          }
        }
      }
      .scopedSubscribe { it.printStackTrace() }
  }

  fun handleRecoverClick() {
    sendSideEffect { OnboardingSideEffect.NavigateToRecoverWallet }
  }

  fun handleLinkClick(uri: Uri) {
    sendSideEffect { OnboardingSideEffect.NavigateToLink(uri) }
  }
}