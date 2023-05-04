package com.asfoundation.wallet.wallet_reward


import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.home.usecases.DisplayConversationListOrChatUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


sealed class RewardSideEffect : SideEffect {
  data class NavigateToSettings(val turnOnFingerprint: Boolean = false) : RewardSideEffect()
}

data class RewardState( val showVipBadge: Boolean = false) : ViewState

@HiltViewModel
class RewardViewModel @Inject constructor(
  private val displayConversationListOrChatUseCase: DisplayConversationListOrChatUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
) : BaseViewModel<RewardState, RewardSideEffect>(initialState()) {

  companion object {
    fun initialState(): RewardState {
      return RewardState()
    }
  }

  init {
   //TODO replace with initializers
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
}
