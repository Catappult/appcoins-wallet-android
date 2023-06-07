package com.appcoins.wallet.core.utils.partners

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = InstallerService::class)
class InstallerSourceService @Inject constructor(@ApplicationContext val context: Context) :
  InstallerService {

  override fun getInstallerPackageName(appPackageName: String): Single<String> {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return Single.just(
          context.packageManager.getInstallSourceInfo(appPackageName).installingPackageName ?: ""
        )
      }
      return Single.just(context.packageManager.getInstallerPackageName(appPackageName) ?: "")
    } catch (e: IllegalArgumentException) {
      return Single.just("")
    }
  }
}