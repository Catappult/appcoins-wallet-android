package com.appcoins.wallet.feature.challengereward.data.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckChallengeRewardPaymentMethodViewModel @Inject constructor(
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
    viewModelScope.launch {
      bdsRepository.getPaymentMethods(
        currencyType = "fiat",
        direct = true,
      ).doOnSuccess { list ->
        if (list.any { it.id == "challenge_reward" })
          viewModelState.update { true }
      }
    }
  }
}
