package com.asfoundation.wallet.billing.partners

import android.content.Context
import io.reactivex.Single

class InstallerSourceService(val context: Context) : InstallerService {

  override fun getInstallerPackageName(appPackageName: String): Single<String> {
    return try {
      Single.just(context.packageManager.getInstallerPackageName(appPackageName) ?: "")
    } catch (e: IllegalArgumentException) {
      Single.error(e)
    }
  }
}