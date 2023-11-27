package com.appcoins.wallet.feature.challengereward.data.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun challengeRewardNavigation(navigation: () -> Unit): (() -> Unit)? {
  val injectionsProvider = hiltViewModel<InjectionsProvider>()
  val vm: ChallengeRewardVisibilityViewModel = viewModel(
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
  val uiState by vm.uiState.collectAsState()
  return uiState
}
