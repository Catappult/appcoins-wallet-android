package com.asfoundation.wallet.main

import androidx.lifecycle.SavedStateHandle
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.RxSchedulers
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.home.usecases.DisplayConversationListOrChatUseCase
import com.asfoundation.wallet.main.use_cases.HasAuthenticationPermissionUseCase
import com.asfoundation.wallet.main.use_cases.IncreaseLaunchCountUseCase
import com.asfoundation.wallet.onboarding.use_cases.ShouldShowOnboardingUseCase
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
import com.asfoundation.wallet.update_required.use_cases.GetAutoUpdateModelUseCase
import com.asfoundation.wallet.update_required.use_cases.HasRequiredHardUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class MainActivitySideEffect : com.appcoins.wallet.ui.arch.SideEffect {
  object NavigateToOnboarding : MainActivitySideEffect()
  object NavigateToNavigationBar : MainActivitySideEffect()
  object NavigateToAutoUpdate : MainActivitySideEffect()
  object NavigateToFingerprintAuthentication : MainActivitySideEffect()
}

object MainActivityState : com.appcoins.wallet.ui.arch.ViewState

@HiltViewModel
class MainActivityViewModel @Inject constructor(
  private val increaseLaunchCount: IncreaseLaunchCountUseCase,
  private val displayConversationListOrChatUseCase: DisplayConversationListOrChatUseCase,
  private val getAutoUpdateModelUseCase: GetAutoUpdateModelUseCase,
  private val hasRequiredHardUpdateUseCase: HasRequiredHardUpdateUseCase,
  private val hasAuthenticationPermissionUseCase: HasAuthenticationPermissionUseCase,
  private val shouldShowOnboardingUseCase: ShouldShowOnboardingUseCase,
  private val savedStateHandle: SavedStateHandle,
  private val rxSchedulers: com.appcoins.wallet.ui.arch.RxSchedulers
) : com.appcoins.wallet.ui.arch.BaseViewModel<MainActivityState, MainActivitySideEffect>(MainActivityState) {

  init {
    handleSupportNotificationClick()
  }

  fun handleInitialNavigation(authComplete: Boolean = false) {
    getAutoUpdateModelUseCase()
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.main)
      .doOnSuccess { (updateVersionCode, updateMinSdk, blackList) ->
        when {
          hasRequiredHardUpdateUseCase(blackList, updateVersionCode, updateMinSdk) ->
            sendSideEffect { MainActivitySideEffect.NavigateToAutoUpdate }
          hasAuthenticationPermissionUseCase() && !authComplete -> {
            sendSideEffect { MainActivitySideEffect.NavigateToFingerprintAuthentication }
          }
          shouldShowOnboardingUseCase() ->
            sendSideEffect { MainActivitySideEffect.NavigateToOnboarding }
          else ->
            sendSideEffect { MainActivitySideEffect.NavigateToNavigationBar }
        }
      }
      .scopedSubscribe()
  }

  private fun handleSupportNotificationClick() {
    val fromSupportNotification = savedStateHandle.get<Boolean>(SUPPORT_NOTIFICATION_CLICK)
    if (fromSupportNotification == true) {
      displayConversationListOrChatUseCase()
    } else {
      // We only count a launch if it did not come from a notification
      increaseLaunchCount()
    }
  }
}