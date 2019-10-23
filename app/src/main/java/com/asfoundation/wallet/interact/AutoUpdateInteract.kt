package com.asfoundation.wallet.interact

import io.reactivex.Single

class AutoUpdateInteract(private val autoUpdateRepository: AutoUpdateRepository,
                         private val localVersionCode: Int, private val currentMinSdk: Int) {

  fun getAutoUpdateModel(invalidateCache: Boolean): Single<AutoUpdateModel> {
    return autoUpdateRepository.loadAutoUpdateModel(invalidateCache)
  }

  fun isUpdateAvailable(versionCode: Int, minSdk: Int): Boolean {
    return localVersionCode < versionCode && currentMinSdk >= minSdk
  }
}
