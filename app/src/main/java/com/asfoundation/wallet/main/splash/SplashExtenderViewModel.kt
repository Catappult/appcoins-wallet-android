package com.asfoundation.wallet.main.splash

import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.gamification.ObserveUserStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class SplashExtenderSideEffect : SideEffect {
  data class ShowVipAnimation(val shouldShow: Boolean) : SplashExtenderSideEffect()
}

object SplashExtenderState : ViewState

@HiltViewModel
class SplashExtenderViewModel @Inject constructor(
  private val observeUserStatsUseCase: ObserveUserStatsUseCase,
  private val rxSchedulers: RxSchedulers
) : BaseViewModel<SplashExtenderState, SplashExtenderSideEffect>(SplashExtenderState) {

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