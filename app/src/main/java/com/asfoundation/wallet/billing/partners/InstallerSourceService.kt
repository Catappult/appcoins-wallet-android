package com.asfoundation.wallet.billing.partners

import android.content.Context
import android.os.Build
import io.reactivex.Single

class InstallerSourceService(val context: Context) : InstallerService {

  override fun getInstallerPackageName(appPackageName: String): Single<String> {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return Single.just(
            context.packageManager.getInstallSourceInfo(appPackageName).installingPackageName ?: "")
      }
      return Single.just(context.packageManager.getInstallerPackageName(appPackageName) ?: "")
    } catch (e: IllegalArgumentException) {
      return Single.just("")
    }
  }
}