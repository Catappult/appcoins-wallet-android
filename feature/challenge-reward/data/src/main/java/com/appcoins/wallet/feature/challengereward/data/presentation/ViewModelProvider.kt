package com.appcoins.wallet.feature.challengereward.data.presentation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.appcoins.wallet.core.analytics.analytics.legacy.ChallengeRewardAnalytics
import com.appcoins.wallet.feature.challengereward.data.model.ChallengeRewardFlowPath
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.reflect.KFunction0

@HiltViewModel
class InjectionsProvider @Inject constructor(
  val challengeRewardAnalytics: ChallengeRewardAnalytics,
) : ViewModel()

@Composable
fun challengeRewardNavigation(
  activity: Activity,
  flowPath: ChallengeRewardFlowPath,
): KFunction0<Unit> {
  val injectionsProvider = hiltViewModel<InjectionsProvider>()
  val vm: ChallengeRewardViewModel = viewModel(
    key = flowPath.id,
    factory = object : Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ChallengeRewardViewModel(
          challengeRewardAnalytics = injectionsProvider.challengeRewardAnalytics,
          activity = activity,
        ) as T
      }
    }
  )
  return { vm.sendChallengeRewardEvent(flowPath) } as KFunction0<Unit>
}