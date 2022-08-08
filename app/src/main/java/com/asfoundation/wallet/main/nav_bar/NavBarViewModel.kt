package com.asfoundation.wallet.main.nav_bar

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.main.use_cases.HasSeenPromotionTooltipUseCase
import com.asfoundation.wallet.onboarding.use_cases.IsOnboardingFromIapUseCase
import com.asfoundation.wallet.promotions.PromotionUpdateScreen
import com.asfoundation.wallet.promotions.PromotionsInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class NavBarSideEffect : SideEffect {
  object ShowPromotionsTooltip : NavBarSideEffect()
  object ShowOnboardingIap : NavBarSideEffect()
}

data class NavBarState(val showPromotionsBadge: Boolean = false) : ViewState

@HiltViewModel
class NavBarViewModel @Inject constructor(
  private val hasSeenPromotionTooltip: HasSeenPromotionTooltipUseCase,
  private val promotionsInteractor: PromotionsInteractor,
  private val isOnboardingFromIapUseCase: IsOnboardingFromIapUseCase
) : BaseViewModel<NavBarState, NavBarSideEffect>(NavBarState()) {

  init {
    handlePromotionUpdateNotification()
    handleOnboardingFromIapScreen()
  }

  /**
   * For now handlePromotionTooltipVisibility() its not being called in the init{} since the tooltip flow will be reevaluated
   * even though there is intention to keep this tooltip in the near future.
   * The ShowPromotionsTooltip SideEffect wont be triggered and the tooltip won't show
   */
  private fun handlePromotionTooltipVisibility() {
    hasSeenPromotionTooltip()
      .doOnSuccess { hasSeen ->
        if (!hasSeen) {
          sendSideEffect { NavBarSideEffect.ShowPromotionsTooltip }
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
      .repeatableScopedSubscribe(NavBarState::showPromotionsBadge.name)
  }

  fun removePromotionsBadge() {
    cancelSubscription(NavBarState::showPromotionsBadge.name)
    setState { copy(showPromotionsBadge = false) }
  }

  private fun handleOnboardingFromIapScreen() {
    if (isOnboardingFromIapUseCase()) {
      sendSideEffect { NavBarSideEffect.ShowOnboardingIap }
    }
  }
}