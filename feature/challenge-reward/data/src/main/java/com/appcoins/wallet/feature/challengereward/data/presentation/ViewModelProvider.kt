package com.appcoins.wallet.feature.challengereward.data.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InjectionsProvider @Inject constructor(
  val bdsRepository: BdsRepository,
) : ViewModel()

private val _challengeRewardVisibilityViewModel = mutableStateOf<ChallengeRewardVisibilityViewModel?>(null)


@Composable
fun challengeRewardNavigation(navigation: () -> Unit): (() -> Unit)? {
  val injectionsProvider = hiltViewModel<InjectionsProvider>()
  _challengeRewardVisibilityViewModel.value = viewModel(
    key = "challengeRewardNavigation",
    factory = object : Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ChallengeRewardVisibilityViewModel(
          bdsRepository = injectionsProvider.bdsRepository,
          navigation = navigation,
        ) as T
      }
    }
  )
  val uiState by _challengeRewardVisibilityViewModel.value!!.uiState.collectAsState()
  return uiState
}
fun getLoadingStateChallengeReward(): MutableState<Boolean> {
  return _challengeRewardVisibilityViewModel.value?.isLoadingChallengerRewardCard ?: mutableStateOf(true)
}
