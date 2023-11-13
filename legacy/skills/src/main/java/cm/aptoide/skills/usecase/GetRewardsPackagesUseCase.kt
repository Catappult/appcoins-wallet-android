package cm.aptoide.skills.usecase

import cm.aptoide.skills.repository.BonusRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetRewardsPackagesUseCase @Inject constructor(private val bonusRepository: BonusRepository) {
  fun getRewardsPackages(): Single<List<String>> {
    return bonusRepository.getRewardsPackages()
      .subscribeOn(Schedulers.io())
  }
}