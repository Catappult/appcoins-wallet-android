package cm.aptoide.pt.app.aptoideinstall


import com.appcoins.wallet.core.network.eskills.install.AptoideInstallPersistence
import rx.Single

class AptoideInstallRepository(val aptoideInstallPersistence: AptoideInstallPersistence) {

  private val aptoideInstallCandidates = ArrayList<String>()

  fun addAptoideInstallCandidate(packageName: String) {
    if (!aptoideInstallCandidates.contains(packageName)) {
      aptoideInstallCandidates.add(packageName)
    }
  }

  fun persistCandidate(packageName: String) {
    if (aptoideInstallCandidates.contains(packageName)) {
      aptoideInstallPersistence.insert(packageName)
      aptoideInstallCandidates.remove(packageName)
    }
  }

  fun isInstalledWithAptoide(packageName: String): Single<Boolean> {
    return aptoideInstallPersistence.isInstalledWithAptoide(packageName)
  }
}