package com.appcoins.wallet.feature.challengereward.data.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.core.analytics.analytics.logging.Log
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChallengeRewardVisibilityViewModel(
  private val bdsRepository: BdsRepository,
  private val navigation: () -> Unit,
) : ViewModel() {
  private val viewModelState = MutableStateFlow<(() -> Unit)?>(null)
  val isLoadingChallengerRewardCard: MutableState<Boolean> = mutableStateOf(true)

  val uiState = viewModelState
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      viewModelState.value
    )

  init {
    viewModelScope.launch {
      bdsRepository.getPaymentMethods(
        currency = "fiat",
        direct = true,
      ).flatMap { methods ->
        if (methods.any { it.id == "challenge_reward" }) {
          isLoadingChallengerRewardCard.value = false
          return@flatMap Single.just(navigation)

        } else {
          isLoadingChallengerRewardCard.value = false
          return@flatMap Single.just(null)
        }
      }
        .subscribeOn(Schedulers.io())
        .subscribe(
          { value ->
            viewModelState.update { value }
          },
          {
            Log.e("ChallengeReward", "Failed loading Payment Methods", it)
            isLoadingChallengerRewardCard.value = false
          }
        )
    }
  }
}
