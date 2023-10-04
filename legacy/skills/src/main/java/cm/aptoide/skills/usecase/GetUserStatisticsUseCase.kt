package cm.aptoide.skills.usecase

import cm.aptoide.skills.model.TimeFrame
import cm.aptoide.skills.repository.StatisticsRepository
import com.appcoins.wallet.core.network.eskills.model.TopNPlayersResponse
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetUserStatisticsUseCase @Inject constructor(
  private val statisticsRepository: StatisticsRepository
) {

  operator fun invoke(
    applicationId: String, userWalletAddress: String, timeFrame: TimeFrame
  ): Single<TopNPlayersResponse> {
    return statisticsRepository.getTopNPlayers(applicationId, userWalletAddress, timeFrame)
      .subscribeOn(Schedulers.io())
  }
}