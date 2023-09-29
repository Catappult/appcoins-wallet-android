package com.appcoins.wallet.feature.challengereward.data.presentation

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.appcoins.wallet.core.analytics.analytics.legacy.ChallengeRewardAnalytics
import com.appcoins.wallet.feature.challengereward.data.ChallengeRewardManager
import com.appcoins.wallet.feature.challengereward.data.model.ChallengeRewardFlowPath

class ChallengeRewardViewModel(
  private val challengeRewardAnalytics: ChallengeRewardAnalytics,
  activity: Activity,
) : ViewModel() {

  init {
    ChallengeRewardManager.create(activity = activity)
  }

  fun sendChallengeRewardEvent(flowPath: ChallengeRewardFlowPath) {
    challengeRewardAnalytics.sendChallengeRewardEvent(flowPath.id)
    ChallengeRewardManager.onNavigate()
  }
}
