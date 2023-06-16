package cm.aptoide.skills.repository

import cm.aptoide.skills.model.TimeFrame
import cm.aptoide.skills.model.TopNPlayersResponse
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(api: GeneralPlayerStats) {
  private val api: GeneralPlayerStats

  init {
    this.api = api
  }

  fun getTopNPlayers(
    applicationId: String?, walletAddress: String?,
    timeFrame: TimeFrame
  ): Single<TopNPlayersResponse> {
    return api.getTopNPlayers(applicationId, walletAddress, timeFrame.name(), RANKINGS_LIMIT)
  }

  companion object {
    private const val RANKINGS_LIMIT = 10
  }
}