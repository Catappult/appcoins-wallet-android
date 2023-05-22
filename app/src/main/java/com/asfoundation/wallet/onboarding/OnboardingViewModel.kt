package com.asfoundation.wallet.onboarding

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.ui.arch.*
import com.appcoins.wallet.ui.arch.data.Async
import com.asfoundation.wallet.app_start.AppStartUseCase
import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.my_wallets.create_wallet.CreateWalletUseCase
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

data class OnboardingState(
  val pageContent: OnboardingContent = OnboardingContent.EMPTY,
  val walletCreationAsync: Async<Unit> = Async.Uninitialized
) : ViewState

@HiltViewModel
class OnboardingViewModel @Inject constructor(
  private val hasWalletUseCase: HasWalletUseCase,
  private val rxSchedulers: RxSchedulers,
  private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
  private val createWalletUseCase: CreateWalletUseCase,
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

  private fun handleLaunchMode(appStartUseCase: AppStartUseCase) {
    viewModelScope.launch {
      when (appStartUseCase.startModes.first()) {
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