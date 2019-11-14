package com.asfoundation.wallet.billing.partners

import android.content.Context
import android.content.pm.PackageManager
import io.reactivex.Single


class OemIdExtractorService(
    private val extractorV1: IExtractOemId,
    private val extractorV2: IExtractOemId) {

  fun extractOemId(packageName: String): Single<String> {
    return extractorV2.extract(packageName)
        .doOnSuccess { extracted -> check(extracted.isNotEmpty()) }
        .onErrorResumeNext(extractorV1.extract(packageName))
  }
}

@Throws(PackageManager.NameNotFoundException::class)
fun getPackageName(context: Context, packageName: String): String {
  return context.packageManager
      .getPackageInfo(packageName, 0)
      .applicationInfo.sourceDir
}
