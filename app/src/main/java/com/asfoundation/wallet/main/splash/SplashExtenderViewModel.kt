package com.asfoundation.wallet.main.splash

import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.asfoundation.wallet.gamification.ObserveUserStatsUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingVipCompletedUseCase
import com.asfoundation.wallet.onboarding.use_cases.ShouldShowOnboardVipUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class SplashExtenderSideEffect : SideEffect {
  data class ShowVipAnimation(val isVip: Boolean, val showVipOnboarding: Boolean) :
      SplashExtenderSideEffect()
}

object SplashExtenderState : ViewState

@HiltViewModel
class SplashExtenderViewModel
@Inject
constructor(
    private val observeUserStatsUseCase: ObserveUserStatsUseCase,
    private val rxSchedulers: RxSchedulers,
    private val shouldShowOnboardVipUseCase: ShouldShowOnboardVipUseCase,
    private val setOnboardingVipCompletedUseCase: SetOnboardingVipCompletedUseCase,
    private val getCurrentWalletUseCase: GetCurrentWalletUseCase
) : BaseViewModel<SplashExtenderState, SplashExtenderSideEffect>(SplashExtenderState) {

  init {
    handleVipStatus()
  }

  private fun handleVipStatus() {
    observeUserStatsUseCase()
        .subscribeOn(rxSchedulers.io)
        .observeOn(rxSchedulers.main)
        .flatMapSingle { gamificationStats ->
          getCurrentWalletUseCase().doOnSuccess { wallet ->
            val isVipLevel =
                gamificationStats.gamificationStatus == GamificationStatus.VIP ||
                    gamificationStats.gamificationStatus == GamificationStatus.VIP_MAX
            sendSideEffect {
              SplashExtenderSideEffect.ShowVipAnimation(
                  isVip = isVipLevel,
                  showVipOnboarding = shouldShowOnboardVipUseCase(isVipLevel, wallet.address))
            }
          }
        }
        .doOnError {
          sendSideEffect {
            SplashExtenderSideEffect.ShowVipAnimation(isVip = false, showVipOnboarding = false)
          }
        }
        .scopedSubscribe()
  }

  fun completeVipOnboarding() {
    getCurrentWalletUseCase()
        .subscribeOn(rxSchedulers.io)
        .doOnSuccess { wallet -> setOnboardingVipCompletedUseCase(wallet.address) }
        .scopedSubscribe()
  }
}
