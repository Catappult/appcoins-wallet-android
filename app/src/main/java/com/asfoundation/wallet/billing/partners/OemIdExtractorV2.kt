package com.asfoundation.wallet.billing.partners

import android.content.Context
import android.content.SharedPreferences
import com.aptoide.apk.injector.extractor.IExtractorCache
import com.aptoide.apk.injector.extractor.domain.IExtract
import com.aptoide.apk.injector.extractor.utils.Environment
import com.asf.wallet.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import java.io.File
import javax.inject.Inject

@BoundTo(supertype = IExtractOemId::class)
class OemIdExtractorV2 @Inject constructor(@ApplicationContext private val context: Context,
                                           private val extractor: IExtract,
                                           private val sharedPreferences: SharedPreferences
                                           ) : IExtractOemId {

  override fun extract(packageName: String): Single<String> {
    return Single.fromCallable {
      getPackageName(context, packageName)
    }
        .flatMap { sourceDir ->
          Single.fromCallable {
            extractor.extract(
                File(sourceDir),
                if (BuildConfig.DEBUG) Environment.DEVELOPMENT else Environment.PRODUCTION ,
                ExtractorCache(sharedPreferences)
            )
          }
        }
        .map {
          it.split(",")[0]
        }
  }

  class ExtractorCache(private val sharedPreferences: SharedPreferences): IExtractorCache {

    override fun put(key: String?, value: String?) {
      sharedPreferences.edit()
        .putString(key, value)
        .apply()
    }

    override fun get(key: String?): String {
      return sharedPreferences.getString(key, "") ?: ""
    }

  }

}
