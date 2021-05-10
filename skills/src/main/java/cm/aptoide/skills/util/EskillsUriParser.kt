package cm.aptoide.skills.util

import android.net.Uri

class EskillsUriParser {
  fun parse(uri: Uri): EskillsUri {
    val scheme = uri.scheme
    val host = uri.host
    val path = uri.path
    val parameters = mutableMapOf<String, String>()
    parameters.apply {
      for (key in uri.queryParameterNames) {
        this[key] = uri.getQueryParameter(key)
      }
    }
    return EskillsUri(scheme!!, host!!, path!!, parameters)
  }
}