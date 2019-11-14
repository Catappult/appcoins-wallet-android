package com.asfoundation.wallet.billing.partners

import android.content.Context
import com.aptoide.apk.injector.extractor.domain.IExtract
import io.reactivex.Single
import java.io.File

class OemIdExtractorV2 @JvmOverloads constructor(private val context: Context,
                                                 private val extractor: IExtract) :
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