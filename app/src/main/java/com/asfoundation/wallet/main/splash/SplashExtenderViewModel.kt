package com.asfoundation.wallet.main.splash

import com.appcoins.wallet.gamification.repository.entity.GamificationStatus
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.RxSchedulers
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.gamification.ObserveUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class SplashExtenderSideEffect : com.appcoins.wallet.ui.arch.SideEffect {
  data class ShowVipAnimation(val shouldShow: Boolean) : SplashExtenderSideEffect()
}

object SplashExtenderState : com.appcoins.wallet.ui.arch.ViewState

@HiltViewModel
class SplashExtenderViewModel @Inject constructor(
  private val observeUserStatsUseCase: ObserveUserStatsUseCase,
  private val rxSchedulers: com.appcoins.wallet.ui.arch.RxSchedulers
) : com.appcoins.wallet.ui.arch.BaseViewModel<SplashExtenderState, SplashExtenderSideEffect>(SplashExtenderState) {

  init {
    handleVipStatus()
  }

  private fun handleVipStatus() {
    observeUserStatsUseCase()
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.main)
      .doOnNext { gamificationStats ->
        val isVipLevel =
          gamificationStats.gamificationStatus == GamificationStatus.VIP || gamificationStats.gamificationStatus == GamificationStatus.VIP_MAX
        sendSideEffect { SplashExtenderSideEffect.ShowVipAnimation(shouldShow = isVipLevel) }
      }
      .scopedSubscribe()
  }
}