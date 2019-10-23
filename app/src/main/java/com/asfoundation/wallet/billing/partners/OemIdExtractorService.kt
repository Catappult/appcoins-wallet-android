package com.asfoundation.wallet.billing.partners

import android.content.Context
import com.aptoide.injectorextractor.Extractor
import io.reactivex.Single
import java.io.File
import java.util.*
import java.util.zip.ZipFile


class OemIdExtractorService(context: Context) {

  val extractorV1: OemIdExtractorV1 = OemIdExtractorV1(context)
  val extractorV2: OemIdExtractorV2 = OemIdExtractorV2(context)

  fun extractOemId(packageName: String): Single<String> {
    return extractorV2.extract(packageName)
        .onErrorResumeNext(extractorV1.extract(packageName))
  }
}

class OemIdExtractorV2(private val context: Context,
                       private val extractor: Extractor = Extractor()) : IExtractOemId {
  override fun extract(packageName: String): Single<String> {
    return Single.fromCallable {
      getPackageName(context, packageName)
    }
        .flatMap { sourceDir -> Single.fromCallable { extractor.extract(File(sourceDir)) } }
        .map { t ->
          t.split(",")[0]
        }
  }
}

class OemIdExtractorV1(private val context: Context) : IExtractOemId {
  override fun extract(packageName: String): Single<String> {
    return Single.create {
      try {
        var oemId = ""
        val sourceDir = getPackageName(context, packageName)
        val myZipFile = ZipFile(sourceDir)
        val entry = myZipFile.getEntry("META-INF/attrib")
        entry?.let {
          val inputStream = myZipFile.getInputStream(entry)
          val properties = Properties()
          properties.load(inputStream)
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

fun getPackageName(context: Context, packageName: String): String {
  return context.packageManager
      .getPackageInfo(packageName, 0)
      .applicationInfo.sourceDir
}

interface IExtractOemId {
  fun extract(packageName: String): Single<String>
}