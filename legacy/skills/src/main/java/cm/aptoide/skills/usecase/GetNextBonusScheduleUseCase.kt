package cm.aptoide.skills.usecase

import com.appcoins.eskills2048.model.NextPrizeSchedule
import com.appcoins.eskills2048.repository.BonusRepository
import com.appcoins.eskills2048.repository.StatisticsTimeFrame
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetNextBonusScheduleUseCase @Inject constructor(bonusRepository: BonusRepository) {
  private val bonusRepository: BonusRepository

  init {
    this.bonusRepository = bonusRepository
  }

  fun execute(timeFrame: StatisticsTimeFrame?): Single<NextPrizeSchedule> {
    return bonusRepository.getNextBonusSchedule(timeFrame)
      .subscribeOn(Schedulers.io())
  }
}