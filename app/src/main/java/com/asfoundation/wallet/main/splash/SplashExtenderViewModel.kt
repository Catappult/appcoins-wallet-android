package com.asfoundation.wallet.main.splash

import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.home.usecases.GetUserLevelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class SplashExtenderSideEffect : SideEffect {
  data class ShowVipAnimation(val shouldShow : Boolean) : SplashExtenderSideEffect()
}

object SplashExtenderState : ViewState

@HiltViewModel
class SplashExtenderViewModel @Inject constructor(
  private val getUserLevelUseCase: GetUserLevelUseCase,
  private val rxSchedulers: RxSchedulers
) : BaseViewModel<SplashExtenderState, SplashExtenderSideEffect>(SplashExtenderState) {

  init {
    handleUserLevel()
  }

  private fun handleUserLevel() {
    getUserLevelUseCase()
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.main)
      .doOnSuccess { userLevel ->
        sendSideEffect { SplashExtenderSideEffect.ShowVipAnimation(userLevel == 9 || userLevel == 10) }
      }
      .scopedSubscribe()
  }
}