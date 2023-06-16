package cm.aptoide.skills.usecase

import com.appcoins.eskills2048.model.BonusHistory
import com.appcoins.eskills2048.repository.BonusRepository
import com.appcoins.eskills2048.repository.StatisticsTimeFrame
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetBonusHistoryUseCase @Inject constructor(bonusRepository: BonusRepository) {
  private val bonusRepository: BonusRepository

  init {
    this.bonusRepository = bonusRepository
  }

  fun execute(
    packageName: String?,
    sku: String?,
    timeFrame: StatisticsTimeFrame?
  ): Single<List<BonusHistory>> {
    return bonusRepository.getBonusHistoryList(packageName, sku, timeFrame)
      .subscribeOn(Schedulers.io())
  }
}