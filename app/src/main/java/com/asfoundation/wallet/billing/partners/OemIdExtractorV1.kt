package com.asfoundation.wallet.billing.partners

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import java.util.*
import java.util.zip.ZipFile
import javax.inject.Inject

@BoundTo(supertype = IExtractOemId::class)
class OemIdExtractorV1 @Inject constructor(@ApplicationContext private val context: Context) :
    IExtractOemId {
  override fun extract(packageName: String): Single<String> {
    return Single.create {
      try {
        var oemId = ""
        val sourceDir =
            getPackageName(context, packageName)
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