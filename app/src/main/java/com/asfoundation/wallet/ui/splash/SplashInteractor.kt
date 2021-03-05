package com.asfoundation.wallet.ui.splash

import com.asfoundation.wallet.abtesting.experiments.balancewallets.BalanceWalletsExperiment
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.repository.ImpressionPreferencesRepositoryType
import com.asfoundation.wallet.viewmodel.AutoUpdateModel
import io.reactivex.Single

class SplashInteractor(private val autoUpdateInteract: AutoUpdateInteract,
                       private val balanceWalletsExperiment: BalanceWalletsExperiment,
                       private val fingerprintPreferencesRepository: FingerprintPreferencesRepositoryContract,
                       private val impressionPreferencesRepositoryType: ImpressionPreferencesRepositoryType) {

  fun getAutoUpdateModel(): Single<AutoUpdateModel> {
    return autoUpdateInteract.getAutoUpdateModel(true)
  }

  fun isHardUpdateRequired(blackList: List<Int>, updateVersionCode: Int,
                           updateMinSdk: Int): Boolean {
    return autoUpdateInteract.isHardUpdateRequired(blackList, updateVersionCode, updateMinSdk)
  }

  //Caches the experiment
  fun retrieveExperiment(): Single<String> = balanceWalletsExperiment.getConfiguration()

  fun hasAuthenticationPermission(): Boolean {
    return fingerprintPreferencesRepository.hasAuthenticationPermission()
  }

  fun shouldShowOnboarding(): Boolean {
    return !impressionPreferencesRepositoryType.hasCompletedOnboarding()
  }

}
