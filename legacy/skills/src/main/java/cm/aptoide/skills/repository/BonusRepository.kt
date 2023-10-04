package cm.aptoide.skills.repository

import cm.aptoide.skills.model.TimeFrame
import com.appcoins.wallet.core.network.eskills.api.BonusPrizeApi
import com.appcoins.wallet.core.network.eskills.model.BonusHistory
import com.appcoins.wallet.core.network.eskills.model.NextPrizeSchedule
import io.reactivex.Single
import javax.inject.Inject

class BonusRepository @Inject constructor(
  private val bonusPrizeApi: BonusPrizeApi
) {

  fun getBonusHistoryList(
    packageName: String, sku: String, timeFrame: TimeFrame
  ): Single<List<BonusHistory>> {
    return bonusPrizeApi.getBonusHistoryList(packageName, sku, timeFrame.name)
  }

  fun getNextBonusSchedule(timeFrame: TimeFrame): Single<NextPrizeSchedule> {
    return bonusPrizeApi.getTimeUntilNextBonus(timeFrame.name)
  }
}