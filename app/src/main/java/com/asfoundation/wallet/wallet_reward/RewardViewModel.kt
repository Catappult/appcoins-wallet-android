package com.asfoundation.wallet.wallet_reward


import androidx.compose.runtime.mutableStateListOf
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.ui.widgets.CardPromotionItem
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.home.usecases.DisplayConversationListOrChatUseCase
import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.promotions.ui.PromotionsState
import com.asfoundation.wallet.promotions.usecases.GetPromotionsUseCase
import com.asfoundation.wallet.promotions.usecases.SetSeenPromotionsUseCase
import com.asfoundation.wallet.wallet.home.HomeState

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


sealed class RewardSideEffect : SideEffect {
  data class NavigateToSettings(val turnOnFingerprint: Boolean = false) : RewardSideEffect()
}

data class RewardState(
  val showVipBadge: Boolean = false,
  val promotionsModelAsync: Async<PromotionsModel> = Async.Uninitialized
) : ViewState

@HiltViewModel
class RewardViewModel @Inject constructor(
  private val displayConversationListOrChatUseCase: DisplayConversationListOrChatUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
  private val getPromotionsUseCase: GetPromotionsUseCase,
  private val setSeenPromotionsUseCase: SetSeenPromotionsUseCase,
  private val rxSchedulers: RxSchedulers
) : BaseViewModel<RewardState, RewardSideEffect>(initialState()) {

  val promotions = mutableStateListOf<CardPromotionItem>()

  companion object {
    fun initialState(): RewardState {
      return RewardState()
    }
  }

  init {
    fetchPromotions()
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

  private fun fetchPromotions() {
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
}
