package com.appcoins.wallet.core.utils.common.applicationinfo

import android.content.Context
import android.graphics.drawable.Drawable
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Helper class to load resources from installed applications
 */
class ApplicationInfoProvider @Inject constructor(@ApplicationContext val context: Context) {

  fun getApplicationInfo(packageName: String): Single<ApplicationInfoModel> {
    return Single.zip(getApplicationName(packageName), getApplicationIcon(packageName),
        BiFunction { appName, appIcon ->
          ApplicationInfoModel(
              packageName, appName, appIcon)
        })
  }

  fun getApplicationIcon(packageName: String): Single<Drawable> {
    return Single.just(packageName)
        .map { pkgName -> context.packageManager.getApplicationIcon(pkgName) }
        .subscribeOn(Schedulers.io())
  }

  fun getApplicationName(packageName: String): Single<String> {
    return Single.just(packageName)
        .map { pkgName ->
          val packageInfo = context.packageManager.getApplicationInfo(pkgName, 0)
          return@map context.packageManager.getApplicationLabel(packageInfo)
              .toString()
        }
        .subscribeOn(Schedulers.io())
  }

  fun getAppInfo(packageName: String): ApplicationInfoModel {
    val packageInfo = context.packageManager.getApplicationInfo(packageName, 0)
    val appName = context.packageManager.getApplicationLabel(packageInfo)
    val icon = context.packageManager.getApplicationIcon(packageName)
    return ApplicationInfoModel(packageName, appName.toString(), icon)
  }

}