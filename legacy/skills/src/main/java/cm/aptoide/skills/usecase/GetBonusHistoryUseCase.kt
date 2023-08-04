package cm.aptoide.skills.usecase

import cm.aptoide.skills.model.TimeFrame
import cm.aptoide.skills.repository.BonusRepository
import com.appcoins.wallet.core.network.eskills.model.BonusHistory
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetBonusHistoryUseCase @Inject constructor(
  private val bonusRepository: BonusRepository
) {

  operator fun invoke(
    packageName: String, sku: String, timeFrame: TimeFrame
  ): Single<List<BonusHistory>> {
    return bonusRepository.getBonusHistoryList(packageName, sku, timeFrame)
      .subscribeOn(Schedulers.io())
  }
}