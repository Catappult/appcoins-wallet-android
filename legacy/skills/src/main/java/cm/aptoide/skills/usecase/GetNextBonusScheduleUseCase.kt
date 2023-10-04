package cm.aptoide.skills.usecase

import cm.aptoide.skills.model.TimeFrame
import cm.aptoide.skills.repository.BonusRepository
import com.appcoins.wallet.core.network.eskills.model.NextPrizeSchedule
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetNextBonusScheduleUseCase @Inject constructor(
  private val bonusRepository: BonusRepository
) {

  fun invoke(timeFrame: TimeFrame): Single<NextPrizeSchedule> {
    return bonusRepository.getNextBonusSchedule(timeFrame).subscribeOn(Schedulers.io())
  }
}