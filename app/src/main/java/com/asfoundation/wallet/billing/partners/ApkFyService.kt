package com.asfoundation.wallet.billing.partners

import android.content.Context
import io.reactivex.Single
import java.util.*
import java.util.zip.ZipFile


class ApkFyService(private val context: Context) {

  fun extractOemId(packageName: String): Single<String> {
    return Single.create {
      try {
        var oemId = ""
        val sourceDir = context.packageManager
            .getPackageInfo(packageName, 0)
            .applicationInfo.sourceDir
        val myZipFile = ZipFile(sourceDir)
        val entry = myZipFile.getEntry("META-INF/attrib")
        if (entry != null) {
          val `is` = myZipFile.getInputStream(entry)
          val properties = Properties()
          properties.load(`is`)
          if (properties.containsKey("oemid")) {
            oemId = properties.getProperty("oemid")
          }
        }
        it.onSuccess(oemId)
      } catch (e: Exception) {
        it.onError(e)
      }
    }
  }

}