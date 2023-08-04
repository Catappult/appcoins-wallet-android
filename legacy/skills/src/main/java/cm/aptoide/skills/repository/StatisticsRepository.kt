package cm.aptoide.skills.repository

import cm.aptoide.skills.model.TimeFrame
import com.appcoins.wallet.core.network.eskills.api.GeneralPlayerStatsApi
import com.appcoins.wallet.core.network.eskills.model.TopNPlayersResponse
import io.reactivex.Single
import javax.inject.Inject

class StatisticsRepository @Inject constructor(
  private val api: GeneralPlayerStatsApi
) {

  fun getTopNPlayers(
    applicationId: String?, walletAddress: String?, timeFrame: TimeFrame
  ): Single<TopNPlayersResponse> {
    return api.getTopNPlayers(applicationId, walletAddress, timeFrame.name, RANKINGS_LIMIT)
  }

  companion object {
    private const val RANKINGS_LIMIT = 10
  }
}