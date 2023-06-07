package com.appcoins.wallet.core.utils.partners

import io.reactivex.Single

interface InstallerService {
  fun getInstallerPackageName(appPackageName: String): Single<String>
}
