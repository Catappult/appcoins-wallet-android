package com.appcoins.wallet.core.utils.properties

import android.net.Uri
import java.util.Locale

class UrlPropertiesFormatter {
  companion object {
    fun addLanguageElementToUrl(
      originalUrl: String,
      pathLanguage: String = Locale.getDefault().toLanguageTag()
    ): Uri = runCatching {
      val uri = Uri.parse(originalUrl)
      val pathLanguageCamelCase = pathLanguage.replace("-", "_")
      val pathSegments = uri.pathSegments.toMutableList()
      pathSegments.add(0, pathLanguageCamelCase)

      uri.buildUpon()
        .path("/" + pathSegments.joinToString("/"))
        .build()
    }
      .onFailure { it.printStackTrace() }
      .getOrDefault(Uri.parse(originalUrl))
  }
}
