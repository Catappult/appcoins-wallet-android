package com.asfoundation.wallet.billing.partners

import android.content.Context
import com.aptoide.apk.injector.extractor.domain.IExtract
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import java.io.File
import javax.inject.Inject

@BoundTo(supertype = IExtractOemId::class)
class OemIdExtractorV2 @Inject constructor(@ApplicationContext private val context: Context,
                                           private val extractor: IExtract) : IExtractOemId {

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