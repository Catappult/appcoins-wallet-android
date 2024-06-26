package com.asfoundation.wallet.main.splash

import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.network.backend.model.GamificationStatus
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.asfoundation.wallet.gamification.ObserveUserStatsUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingVipVisualisationStateUseCase
import com.asfoundation.wallet.onboarding.use_cases.ShouldShowOnboardVipUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SplashExtenderViewModel
@Inject
constructor(
  private val observeUserStatsUseCase: ObserveUserStatsUseCase,
  private val rxSchedulers: RxSchedulers,
  private val shouldShowOnboardVipUseCase: ShouldShowOnboardVipUseCase,
  private val setOnboardingVipVisualisationStateUseCase:
  SetOnboardingVipVisualisationStateUseCase,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
  var uiState: StateFlow<UiState> = _uiState

  init {
    handleVipStatus()
  }

  private fun handleVipStatus() =
    observeUserStatsUseCase()
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.main)
      .flatMapSingle { gamificationStats ->
        val isVipLevel =
          gamificationStats.gamificationStatus == GamificationStatus.VIP ||
              gamificationStats.gamificationStatus == GamificationStatus.VIP_MAX

        getCurrentWalletUseCase().map { wallet ->
          if (gamificationStats.gamificationStatus == GamificationStatus.NONE) UiState.Fail
          else
            UiState.Success(
              isVip = isVipLevel,
              showVipOnboarding = shouldShowOnboardVipUseCase(isVipLevel, wallet.address)
            )
        }
      }
      .doOnSubscribe { _uiState.value = UiState.Loading() }
      .doOnError { _uiState.value = UiState.Fail }
      .subscribe(
        { successState -> _uiState.value = successState },
        { _ -> _uiState.value = UiState.Fail })

  fun setOnboardingVipVisualisationState(firstVipOnboarding: Boolean) =
    getCurrentWalletUseCase()
      .subscribeOn(rxSchedulers.io)
      .observeOn(rxSchedulers.main)
      .subscribe(
        { wallet ->
          setOnboardingVipVisualisationStateUseCase(wallet.address, firstVipOnboarding)
        },
        { _uiState.value = UiState.Fail })

  sealed class UiState {
    object Idle : UiState()

    data class Loading(val isVip: Boolean = false) : UiState()

    object Fail : UiState()

    data class Success(val isVip: Boolean, val showVipOnboarding: Boolean) : UiState()
  }
}
