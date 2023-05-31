package com.asfoundation.wallet.main.nav_bar

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.asf.wallet.R
import com.asfoundation.wallet.app_start.AppStartUseCase
import com.asfoundation.wallet.app_start.StartMode
import com.asfoundation.wallet.main.use_cases.HasSeenPromotionTooltipUseCase
import com.asfoundation.wallet.main.use_cases.IsNewVipUseCase
import com.asfoundation.wallet.main.use_cases.SetVipPromotionsSeenUseCase
import com.asfoundation.wallet.promotions.PromotionUpdateScreen
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.ui.bottom_navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavBarSideEffect : SideEffect {
  object ShowPromotionsTooltip : NavBarSideEffect()
  object ShowOnboardingGPInstall : NavBarSideEffect()
  object ShowOnboardingPendingPayment : NavBarSideEffect()
}

data class NavBarState(
  val showPromotionsBadge: Boolean = false,
  val shouldShowVipCallout: Boolean = false
) : ViewState

@HiltViewModel
class NavBarViewModel @Inject constructor(
  private val hasSeenPromotionTooltip: HasSeenPromotionTooltipUseCase,
  private val promotionsInteractor: PromotionsInteractor,
  private val isNewVipUseCase: IsNewVipUseCase,
  private val setVipPromotionsSeenUseCase: SetVipPromotionsSeenUseCase,
  private val appStartUseCase: AppStartUseCase
) : BaseViewModel<NavBarState, NavBarSideEffect>(NavBarState()) {

  val clickedItem: MutableState<Int> = mutableStateOf(Destinations.HOME.ordinal)

  init {
    handlePromotionUpdateNotification()
    handleOnboardingFromGameScreen()
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

  private fun handleOnboardingFromGameScreen() {
    viewModelScope.launch {
      when (appStartUseCase.startModes.first()) {
        is StartMode.PendingPurchaseFlow -> {
          sendSideEffect { NavBarSideEffect.ShowOnboardingPendingPayment }
        }
        is StartMode.GPInstall -> {
          sendSideEffect { NavBarSideEffect.ShowOnboardingGPInstall }
        }
        else -> Unit
      }
    }
  }

  fun handleVipCallout() {
    isNewVipUseCase()
      .doOnNext { isNewVip ->
        setState { copy(shouldShowVipCallout = isNewVip) }
      }
      .repeatableScopedSubscribe(NavBarState::shouldShowVipCallout.name)
  }

  fun vipPromotionsSeen() {
    setVipPromotionsSeenUseCase(true)
      .scopedSubscribe()
  }

  fun navigationItems() = listOf(
    NavigationItem(
      destination = Destinations.HOME,
      label = R.string.intro_home_button,
      icon = R.drawable.ic_home,
      selected = true
    ),
    NavigationItem(
      destination = Destinations.REWARDS,
      label = R.string.intro_rewards_button,
      icon = R.drawable.ic_rewards,
      selected = false
    )
  )
}
