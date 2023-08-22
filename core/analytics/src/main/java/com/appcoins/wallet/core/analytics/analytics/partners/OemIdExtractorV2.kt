package com.appcoins.wallet.core.analytics.analytics.partners

import android.content.Context
import com.appcoins.wallet.core.analytics.BuildConfig
import com.appcoins.wallet.sharedpreferences.OemIdPreferencesDataSource
import com.aptoide.apk.injector.extractor.IExtractorCache
import com.aptoide.apk.injector.extractor.domain.IExtract
import com.aptoide.apk.injector.extractor.utils.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import java.io.File
import javax.inject.Inject

@BoundTo(supertype = IExtractOemId::class)
class OemIdExtractorV2 @Inject constructor(@ApplicationContext private val context: Context,
                                           private val extractor: IExtract,
                                           private val sharedPreferences: OemIdPreferencesDataSource
                                           ) : IExtractOemId {

  override fun extract(packageName: String): Single<String> {
    return Single.fromCallable {
      getPackageName(context, packageName)
    }
        .flatMap { sourceDir ->
          Single.fromCallable {
            extractor.extract(
                File(sourceDir),
              if (BuildConfig.DEBUG) Environment.DEVELOPMENT else Environment.PRODUCTION,
              ExtractorCache(sharedPreferences)
            )
          }
        }
      .map {
        it.split(",")[0]
      }
  }

  class ExtractorCache(private val sharedPreferences: OemIdPreferencesDataSource) :
    IExtractorCache {
    override fun put(key: String?, value: String?) = sharedPreferences.putOemId(key, value)

    override fun get(key: String?) = sharedPreferences.getOemId(key) ?: ""
  }
}
