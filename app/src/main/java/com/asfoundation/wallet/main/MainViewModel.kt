package com.asfoundation.wallet.main

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.main.usecases.HasSeenPromotionTooltipUseCase
import com.asfoundation.wallet.main.usecases.IncreaseLaunchCountUseCase
import com.asfoundation.wallet.promotions.PromotionUpdateScreen
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.support.SupportInteractor

sealed class MainSideEffect : SideEffect {
  object ShowPromotionsTooltip : MainSideEffect()
}

data class MainState(val showPromotionsBadge: Boolean = false) : ViewState

class MainViewModel(
    private val data: MainData,
    private val hasSeenPromotionTooltip: HasSeenPromotionTooltipUseCase,
    private val increaseLaunchCount: IncreaseLaunchCountUseCase,
    private val promotionsInteractor: PromotionsInteractor,
    private val supportInteractor: SupportInteractor
) : BaseViewModel<MainState, MainSideEffect>(MainState()) {

  init {
    handleSupportNotificationClick()
    handlePromotionTooltipVisibility()
    handlePromotionUpdateNotification()
  }

  private fun handleSupportNotificationClick() {
    if (data.fromSupportNotificationClick) {
      supportInteractor.displayConversationListOrChat()
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