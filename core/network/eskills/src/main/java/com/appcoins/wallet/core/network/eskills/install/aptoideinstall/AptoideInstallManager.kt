package cm.aptoide.pt.app.aptoideinstall


import com.appcoins.wallet.core.network.eskills.install.AptoideInstalledAppsRepository
import rx.Single

class AptoideInstallManager(
  val aptoideInstalledAppsRepository: AptoideInstalledAppsRepository,
  val aptoideInstallRepository: AptoideInstallRepository
) {

  fun addAptoideInstallCandidate(packageName: String) {
    aptoideInstallRepository.addAptoideInstallCandidate(packageName)
  }

  fun persistCandidate(packageName: String) {
    aptoideInstallRepository.persistCandidate(packageName)
  }

  fun isInstalledWithAptoide(packageName: String): Single<Boolean> {
    if (isSplitInstalledWithAptoide(packageName)) {
      return Single.just(true)
    }
    return aptoideInstallRepository.isInstalledWithAptoide(packageName)
  }

  private fun isSplitInstalledWithAptoide(packageName: String): Boolean {
    return false
  }
}