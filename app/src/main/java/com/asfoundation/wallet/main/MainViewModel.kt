package com.asfoundation.wallet.main

import androidx.lifecycle.SavedStateHandle
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.home.usecases.DisplayConversationListOrChatUseCase
import com.asfoundation.wallet.main.usecases.HasSeenPromotionTooltipUseCase
import com.asfoundation.wallet.main.usecases.IncreaseLaunchCountUseCase
import com.asfoundation.wallet.onboarding.use_cases.IsOnboardingFromIapUseCase
import com.asfoundation.wallet.promotions.PromotionUpdateScreen
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.support.SupportNotificationProperties
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class MainSideEffect : SideEffect {
  object ShowPromotionsTooltip : MainSideEffect()
  object ShowOnboardingIapScreen : MainSideEffect()
}

data class MainState(val showPromotionsBadge: Boolean = false) : ViewState

@HiltViewModel
class MainViewModel @Inject constructor(
  private val hasSeenPromotionTooltip: HasSeenPromotionTooltipUseCase,
  private val increaseLaunchCount: IncreaseLaunchCountUseCase,
  private val promotionsInteractor: PromotionsInteractor,
  private val displayConversationListOrChatUseCase: DisplayConversationListOrChatUseCase,
  private val isOnboardingFromIapUseCase: IsOnboardingFromIapUseCase,
  private val savedStateHandle: SavedStateHandle
) : BaseViewModel<MainState, MainSideEffect>(MainState()) {

  init {
    handleSupportNotificationClick()
    handlePromotionTooltipVisibility()
    handlePromotionUpdateNotification()
    handleOnboardingIap()
  }

  private fun handleSupportNotificationClick() {
    val fromSupportNotification =
      savedStateHandle.get<Boolean>(SupportNotificationProperties.SUPPORT_NOTIFICATION_CLICK)
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

  private fun handleOnboardingIap() {
    if (isOnboardingFromIapUseCase()) {
      sendSideEffect { MainSideEffect.ShowOnboardingIapScreen }
    }
  }

  fun navigatedToPromotions() {
    cancelSubscription(MainState::showPromotionsBadge.name)
    setState { copy(showPromotionsBadge = false) }
  }
}