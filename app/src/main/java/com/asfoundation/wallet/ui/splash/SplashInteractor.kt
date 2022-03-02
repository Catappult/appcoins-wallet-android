package com.asfoundation.wallet.ui.splash

import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.viewmodel.AutoUpdateModel
import io.reactivex.Single
import javax.inject.Inject

class SplashInteractor @Inject constructor(private val autoUpdateInteract: AutoUpdateInteract,
                       private val fingerprintPreferencesRepository: FingerprintPreferencesRepositoryContract,
                       private val preferencesRepositoryType: PreferencesRepositoryType) {

  fun getAutoUpdateModel(): Single<AutoUpdateModel> {
    return autoUpdateInteract.getAutoUpdateModel(true)
  }

  fun isHardUpdateRequired(blackList: List<Int>, updateVersionCode: Int,
                           updateMinSdk: Int): Boolean {
    return autoUpdateInteract.isHardUpdateRequired(blackList, updateVersionCode, updateMinSdk)
  }

  fun hasAuthenticationPermission(): Boolean {
    return fingerprintPreferencesRepository.hasAuthenticationPermission()
  }

  fun shouldShowOnboarding(): Boolean {
    return !preferencesRepositoryType.hasCompletedOnboarding()
  }

}
