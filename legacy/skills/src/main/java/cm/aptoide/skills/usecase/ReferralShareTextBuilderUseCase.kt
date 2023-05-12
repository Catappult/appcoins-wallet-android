package cm.aptoide.skills.usecase

import cm.aptoide.skills.repository.AppMetaDataRepository
import com.appcoins.wallet.core.network.eskills.model.AppData
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ReferralShareTextBuilderUseCase @Inject constructor(
  private val appMetaDataRepository: AppMetaDataRepository
) {

  operator fun invoke(
    packageName: String,
  ): Single<AppData> {
    return appMetaDataRepository.getMeta(packageName)
      .subscribeOn(Schedulers.io())
      .map{it.data}
  }
}