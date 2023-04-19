package cm.aptoide.skills.usecase

import cm.aptoide.skills.repository.AppMetaDataRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ReferralShareTextBuilderUseCase @Inject constructor(
  private val appMetaDataRepository: AppMetaDataRepository
) {

  operator fun invoke(
    packageName: String
  ): Single<String> {
    return appMetaDataRepository.getMeta(packageName)
      .subscribeOn(Schedulers.io())
      .map{String.format("https://%s.en.aptoide.com",it.data.uname)}
  }
}