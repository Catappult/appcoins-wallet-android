package com.asfoundation.wallet.wallet_reward

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.appcoins.wallet.core.analytics.analytics.compatible_apps.CompatibleAppsAnalytics
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.Dispatchers
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetSelectedCurrencySymbolUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.appcoins.wallet.ui.widgets.ActiveCardPromoCodeItem
import com.appcoins.wallet.ui.widgets.CardPromotionItem
import com.asfoundation.wallet.home.PromotionsState
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.promotions.model.VipReferralInfo
import com.asfoundation.wallet.promotions.usecases.GetPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenPromotionsUseCase
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.webview_gamification.usecases.GenerateWebGamificationUrlUseCase
import com.github.michaelbull.result.get
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

sealed class RewardSideEffect : SideEffect {
  data class NavigateToSettings(val turnOnFingerprint: Boolean = false) : RewardSideEffect()
  data class NavigateToWebGamification(val url: String) : RewardSideEffect()
}

data class RewardState(
  val promotionsModelAsync: Async<PromotionsModel> = Async.Uninitialized,
  val promotionsGamificationStatsAsync: Async<PromotionsGamificationStats> = Async.Uninitialized,
  val walletInfoAsync: Async<WalletInfo> = Async.Uninitialized,
  val selectedCurrency: Async<String?> = Async.Uninitialized,
) : ViewState

@HiltViewModel
class RewardViewModel @Inject constructor(
  private val displayChatUseCase: DisplayChatUseCase,
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val getPromotionsUseCase: GetPromotionsUseCase,
  private val setSeenPromotionsUseCase: SetSeenPromotionsUseCase,
  private val gamificationInteractor: GamificationInteractor,
  private val generateWebGamificationUrlUseCase: GenerateWebGamificationUrlUseCase,
  private val rxSchedulers: RxSchedulers,
  private val dispatchers: Dispatchers,
  private val compatibleAppsAnalytics: CompatibleAppsAnalytics,
  private val getSelectedCurrencySymbolUseCase: GetSelectedCurrencySymbolUseCase
) : BaseViewModel<RewardState, RewardSideEffect>(initialState()) {

  val promotions = mutableStateListOf<CardPromotionItem>()
  val gamificationHeaderModel =
    mutableStateOf<GamificationHeaderModel?>(GamificationHeaderModel.emptySkeletonLoadingState())
  val vipReferralModel = mutableStateOf<VipReferralInfo?>(null)
  val activePromoCode = mutableStateOf<ActiveCardPromoCodeItem?>(null)

  companion object {
    fun initialState(): RewardState {
      return RewardState()
    }
  }

  fun onSettingsClick() {
    sendSideEffect { RewardSideEffect.NavigateToSettings() }
  }

  fun showSupportScreen() {
    displayChatUseCase()
  }

  fun getCurrency() {
    rxSingle(dispatchers.io) { getSelectedCurrencySymbolUseCase(bypass = true) }.toObservable()
      .subscribeOn(rxSchedulers.io)
      .map { it.get() }
      .asAsyncToState { copy(selectedCurrency = it) }
      .scopedSubscribe()
  }

  fun fetchPromotions() {
    getPromotionsUseCase()
      .subscribeOn(rxSchedulers.io)
      .asAsyncToState(RewardState::promotionsModelAsync) { copy(promotionsModelAsync = it) }
      .doOnNext { promotionsModel ->
        if (promotionsModel.error == null) {
          setSeenPromotionsUseCase(promotionsModel.promotions, promotionsModel.wallet.address)
        }
      }
      .repeatableScopedSubscribe(PromotionsState::promotionsModelAsync.name) { e ->
        e.printStackTrace()
      }
  }

  fun fetchGamificationStats() {
    gamificationInteractor
      .getUserStats()
      .subscribeOn(rxSchedulers.io)
      .asAsyncToState { copy(promotionsGamificationStatsAsync = it) }
      .scopedSubscribe()
  }

  fun fetchWalletInfo() {
    getWalletInfoUseCase
      .invoke(null, false)
      .subscribeOn(rxSchedulers.io)
      .asAsyncToState { copy(walletInfoAsync = it) }
      .scopedSubscribe()
  }

  fun isLoadingOrIdlePromotionState(): Boolean {
    return state.promotionsModelAsync == Async.Loading(null) ||
        state.promotionsModelAsync == Async.Uninitialized
  }

  fun referenceSendPromotionClickEvent(): (String?, String) -> Unit {
    return compatibleAppsAnalytics::sendPromotionClickEvent
  }

  fun getUrlAndOpenGamification() {
    gamificationInteractor.getUserStats()
      .flatMapSingle { userStats ->
        generateWebGamificationUrlUseCase(
          userStatsBonusReceived = userStats.totalEarned,
          userStatsAmount = userStats.totalSpend,
          userStatsLevel = userStats.level
        )
      }
      .subscribeOn(rxSchedulers.io)
      .doOnNext { url ->
        sendSideEffect { RewardSideEffect.NavigateToWebGamification(url) }
      }
      .scopedSubscribe()
  }
}
