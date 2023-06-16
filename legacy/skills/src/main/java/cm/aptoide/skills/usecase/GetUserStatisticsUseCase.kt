package cm.aptoide.skills.usecase

import cm.aptoide.skills.model.TimeFrame
import cm.aptoide.skills.model.TopNPlayersResponse
import cm.aptoide.skills.repository.StatisticsRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetUserStatisticsUseCase @Inject constructor(statisticsRepository: StatisticsRepository) {
  private val statisticsRepository: StatisticsRepository

  init {
    this.statisticsRepository = statisticsRepository
  }

  fun execute(
    applicationId: String?, userWalletAddress: String?,
    timeFrame: TimeFrame?
  ): Single<TopNPlayersResponse> {
    return statisticsRepository.getTopNPlayers(applicationId, userWalletAddress, timeFrame!!)
      .subscribeOn(Schedulers.io())
  }
}