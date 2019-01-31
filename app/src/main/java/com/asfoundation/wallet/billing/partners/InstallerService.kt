package com.asfoundation.wallet.billing.partners

import io.reactivex.Single

interface InstallerService {
  fun getInstallerPackageName(appPackageName: String): Single<String>
}
