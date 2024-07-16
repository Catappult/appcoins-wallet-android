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
import com.asfoundation.wallet.onboarding.CachedTransactionRepository.Companion.PAYMENT_TYPE_OSP
import com.asfoundation.wallet.onboarding.CachedTransactionRepository.Companion.PAYMENT_TYPE_SDK
import com.asfoundation.wallet.promotions.PromotionUpdateScreen
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.ui.bottom_navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavBarSideEffect : SideEffect {
  object ShowOnboardingGPInstall : NavBarSideEffect()

  object ShowOnboardingPendingPayment : NavBarSideEffect()

  object ShowAskNotificationPermission : NavBarSideEffect()

  data class ShowOnboardingRecoverGuestWallet(val backup: String) : NavBarSideEffect()
}

data class NavBarState(
  val showPromotionsBadge: Boolean = false,
) : ViewState

@HiltViewModel
class NavBarViewModel
@Inject
constructor(
  private val promotionsInteractor: PromotionsInteractor,
  private val appStartUseCase: AppStartUseCase
) : BaseViewModel<NavBarState, NavBarSideEffect>(NavBarState()) {

  val clickedItem: MutableState<Int> = mutableStateOf(Destinations.HOME.ordinal)

  init {
    handlePromotionUpdateNotification()
    handleOnboardingFromGameScreen()
  }

  /**
   * For now handlePromotionTooltipVisibility() its not being called in the init{} since the tooltip
   * flow will be reevaluated even though there is intention to keep this tooltip in the near
   * future. The ShowPromotionsTooltip SideEffect wont be triggered and the tooltip won't show
   */
  private fun handlePromotionUpdateNotification() {
    promotionsInteractor
      .hasAnyPromotionUpdate(PromotionUpdateScreen.TRANSACTIONS)
      .doOnSuccess { hasPromotionUpdate ->
        setState { copy(showPromotionsBadge = hasPromotionUpdate) }
      }
      .toObservable()
      .repeatableScopedSubscribe(NavBarState::showPromotionsBadge.name)
  }

  private fun handleOnboardingFromGameScreen() {
    viewModelScope.launch {
      when (val startMode = appStartUseCase.startModes.first()) {
        is StartMode.PendingPurchaseFlow -> {
          if (startMode.type == PAYMENT_TYPE_OSP) {
            sendSideEffect { NavBarSideEffect.ShowOnboardingPendingPayment }
          } else if (startMode.type == PAYMENT_TYPE_SDK) {
            sendSideEffect { NavBarSideEffect.ShowOnboardingRecoverGuestWallet(startMode.backup!!) }
          }
        }

        is StartMode.GPInstall -> sendSideEffect { NavBarSideEffect.ShowOnboardingGPInstall }
        is StartMode.RestoreGuestWalletFlow -> {
          sendSideEffect { NavBarSideEffect.ShowOnboardingRecoverGuestWallet(startMode.backup) }
        }

        StartMode.Regular, StartMode.Subsequent -> {
          sendSideEffect { NavBarSideEffect.ShowAskNotificationPermission }
        }
      }
    }
  }

  fun navigationItems() =
    listOf(
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
