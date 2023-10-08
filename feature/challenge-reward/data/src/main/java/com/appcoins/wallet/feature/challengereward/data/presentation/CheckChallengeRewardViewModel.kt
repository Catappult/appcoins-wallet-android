package com.appcoins.wallet.feature.challengereward.data.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckChallengeRewardViewModel @Inject constructor(
  private val bdsRepository: BdsRepository,
) : ViewModel() {
  private val viewModelState = MutableStateFlow(false)

  val uiState = viewModelState
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      viewModelState.value
    )

  init {
    handleHasChallengeReward()
  }

  fun handleHasChallengeReward() {
    viewModelScope.launch {
      viewModelState.update {
        bdsRepository.getPaymentMethods(
          currency = "fiat",
          direct = true,
        ).flatMap { methods ->
          Single.just(methods.any { it.id == "challenge_reward" })
        }.subscribeOn(Schedulers.io()).blockingGet()
      }
    }
  }
}
