package com.asfoundation.wallet.wallet_reward


import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.appcoins.wallet.core.analytics.analytics.legacy.ChallengeRewardAnalytics
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.challengereward.data.ChallengeRewardManager
import com.appcoins.wallet.feature.challengereward.data.model.ChallengeRewardFlowPath
import com.appcoins.wallet.ui.widgets.ActiveCardPromoCodeItem
import com.appcoins.wallet.ui.widgets.CardPromotionItem
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.home.usecases.DisplayConversationListOrChatUseCase
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.promotions.model.VipReferralInfo
import com.asfoundation.wallet.promotions.ui.PromotionsState
import com.asfoundation.wallet.promotions.usecases.GetPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenPromotionsUseCase
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


sealed class RewardSideEffect : SideEffect {
  data class NavigateToSettings(val turnOnFingerprint: Boolean = false) : RewardSideEffect()
}

data class RewardState(
  val showVipBadge: Boolean = false,
  val promotionsModelAsync: Async<PromotionsModel> = Async.Uninitialized,
  val promotionsGamificationStatsAsync: Async<PromotionsGamificationStats> = Async.Uninitialized
) : ViewState

@HiltViewModel
class RewardViewModel @Inject constructor(
  private val displayConversationListOrChatUseCase: DisplayConversationListOrChatUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
  private val getPromotionsUseCase: GetPromotionsUseCase,
  private val setSeenPromotionsUseCase: SetSeenPromotionsUseCase,
  private val gamificationInteractor: GamificationInteractor,
  private val challengeRewardAnalytics: ChallengeRewardAnalytics,
  private val rxSchedulers: RxSchedulers,
) : BaseViewModel<RewardState, RewardSideEffect>(initialState()) {

  val promotions = mutableStateListOf<CardPromotionItem>()
  val gamificationHeaderModel = mutableStateOf<GamificationHeaderModel?>(null)
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

  fun showSupportScreen(fromNotification: Boolean) {
    if (fromNotification) {
      displayConversationListOrChatUseCase
    } else {
      displayChatUseCase()
    }
  }

  fun fetchPromotions() {
    getPromotionsUseCase()
      .subscribeOn(rxSchedulers.io)
      .asAsyncToState(RewardState::promotionsModelAsync) {
        copy(promotionsModelAsync = it)
      }
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
    gamificationInteractor.getUserStats()
      .subscribeOn(rxSchedulers.io)
      .asAsyncToState { copy(promotionsGamificationStatsAsync = it) }
      .scopedSubscribe()
  }

  fun sendChallengeRewardEvent(flowPath: ChallengeRewardFlowPath) {
    challengeRewardAnalytics.sendChallengeRewardEvent(flowPath.id)
    ChallengeRewardManager.onNavigate()
  }
}
