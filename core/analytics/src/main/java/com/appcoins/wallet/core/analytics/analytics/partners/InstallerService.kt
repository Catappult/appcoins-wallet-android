package com.appcoins.wallet.core.analytics.analytics.partners

import io.reactivex.Single

interface InstallerService {
  fun getInstallerPackageName(appPackageName: String): Single<String>
}
