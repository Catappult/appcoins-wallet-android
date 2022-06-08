package com.asfoundation.wallet.main

import androidx.lifecycle.SavedStateHandle
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.home.usecases.DisplayConversationListOrChatUseCase
import com.asfoundation.wallet.main.use_cases.HasAuthenticationPermissionUseCase
import com.asfoundation.wallet.main.use_cases.HasSeenPromotionTooltipUseCase
import com.asfoundation.wallet.main.use_cases.IncreaseLaunchCountUseCase
import com.asfoundation.wallet.onboarding.use_cases.IsOnboardingFromIapUseCase
import com.asfoundation.wallet.onboarding.use_cases.ShouldShowOnboardingUseCase
import com.asfoundation.wallet.promotions.PromotionUpdateScreen
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.support.SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK
import com.asfoundation.wallet.update_required.use_cases.GetAutoUpdateModelUseCase
import com.asfoundation.wallet.update_required.use_cases.HasRequiredHardUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class MainSideEffect : SideEffect {
  object NavigateToOnboarding : MainSideEffect()
  object NavigateToOnboardingIap : MainSideEffect()
  object NavigateToHome : MainSideEffect()
  object NavigateToAutoUpdate : MainSideEffect()
  object NavigateToFingerprintAuthentication : MainSideEffect()
  object ShowPromotionsTooltip : MainSideEffect()
}

data class MainState(val showPromotionsBadge: Boolean = false) : ViewState

@HiltViewModel
class MainViewModel @Inject constructor(
  private val hasSeenPromotionTooltip: HasSeenPromotionTooltipUseCase,
  private val increaseLaunchCount: IncreaseLaunchCountUseCase,
  private val promotionsInteractor: PromotionsInteractor,
  private val displayConversationListOrChatUseCase: DisplayConversationListOrChatUseCase,
  private val getAutoUpdateModelUseCase: GetAutoUpdateModelUseCase,
  private val hasRequiredHardUpdateUseCase: HasRequiredHardUpdateUseCase,
  private val hasAuthenticationPermissionUseCase: HasAuthenticationPermissionUseCase,
  private val shouldShowOnboardingUseCase: ShouldShowOnboardingUseCase,
  private val isOnboardingFromIapUseCase: IsOnboardingFromIapUseCase,
  private val savedStateHandle: SavedStateHandle,
  private val rxSchedulers: RxSchedulers
) : BaseViewModel<MainState, MainSideEffect>(MainState()) {

  init {
    handleSupportNotificationClick()
    handlePromotionTooltipVisibility()
    handlePromotionUpdateNotification()
  }

  fun handleInitialNavigation(authComplete : Boolean = false) {
    getAutoUpdateModelUseCase()
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.main)
      .doOnSuccess { (updateVersionCode, updateMinSdk, blackList) ->
        when {
          hasRequiredHardUpdateUseCase(blackList, updateVersionCode, updateMinSdk) ->
            sendSideEffect { MainSideEffect.NavigateToAutoUpdate }
          hasAuthenticationPermissionUseCase() && !authComplete -> {
            sendSideEffect { MainSideEffect.NavigateToFingerprintAuthentication }
          }
          isOnboardingFromIapUseCase() && shouldShowOnboardingUseCase()->
            sendSideEffect { MainSideEffect.NavigateToOnboardingIap }
          shouldShowOnboardingUseCase() ->
            sendSideEffect { MainSideEffect.NavigateToOnboarding }
          else ->
            sendSideEffect { MainSideEffect.NavigateToHome }
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

  private fun handlePromotionTooltipVisibility() {
    hasSeenPromotionTooltip()
      .doOnSuccess { hasSeen ->
        if (!hasSeen) {
          sendSideEffect { MainSideEffect.ShowPromotionsTooltip }
        }
      }
      .scopedSubscribe()
  }

  private fun handlePromotionUpdateNotification() {
    promotionsInteractor.hasAnyPromotionUpdate(PromotionUpdateScreen.TRANSACTIONS)
      .doOnSuccess { hasPromotionUpdate ->
        setState { copy(showPromotionsBadge = hasPromotionUpdate) }
      }
      .toObservable()
      .repeatableScopedSubscribe(MainState::showPromotionsBadge.name)
  }

  fun navigatedToPromotions() {
    cancelSubscription(MainState::showPromotionsBadge.name)
    setState { copy(showPromotionsBadge = false) }
  }
}