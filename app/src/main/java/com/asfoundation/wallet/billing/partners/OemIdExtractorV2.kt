package com.asfoundation.wallet.billing.partners

import android.content.Context
import com.aptoide.apk.injector.extractor.data.Extractor
import com.aptoide.apk.injector.extractor.data.ExtractorV1
import com.aptoide.apk.injector.extractor.data.ExtractorV2
import io.reactivex.Single
import java.io.File

class OemIdExtractorV2 @JvmOverloads constructor(private val context: Context,
                                                 private val extractor: Extractor = Extractor(
                                                     ExtractorV1(), ExtractorV2())) :
    IExtractOemId {

  override fun extract(packageName: String): Single<String> {
    return Single.fromCallable {
      getPackageName(context, packageName)
    }
        .flatMap { sourceDir ->
          Single.fromCallable {
            extractor.extract(
                File(sourceDir))
          }
        }
        .map {
          it.split(",")[0]
        }
  }
}