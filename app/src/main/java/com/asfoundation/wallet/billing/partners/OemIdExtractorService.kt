package com.asfoundation.wallet.billing.partners

import android.content.Context
import android.content.pm.PackageManager
import io.reactivex.Single


class OemIdExtractorService(
    oemIdExtractorV1: IExtractOemId,
    oemIdExtractorV2: IExtractOemId) {

  private val extractorV1: IExtractOemId = oemIdExtractorV1
  private val extractorV2: IExtractOemId = oemIdExtractorV2

  fun extractOemId(packageName: String): Single<String> {
    return extractorV2.extract(packageName)
        .onErrorResumeNext(extractorV1.extract(packageName))
  }
}

@Throws(PackageManager.NameNotFoundException::class)
fun getPackageName(context: Context, packageName: String): String {
  return context.packageManager
      .getPackageInfo(packageName, 0)
      .applicationInfo.sourceDir
}
