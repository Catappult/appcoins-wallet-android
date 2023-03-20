package com.appcoins.wallet.core.utils.android_common

import java.io.UnsupportedEncodingException

class UrlUtmParser {

  companion object {

    @Throws(UnsupportedEncodingException::class)
    fun splitQuery(urlParams: String): Map<String, MutableList<String?>> {
      val queryPairs: MutableMap<String, MutableList<String?>> = LinkedHashMap()
      val pairs: List<String> = urlParams.split("&")
      for (pair in pairs) {
        val keyVal: List<String> = pair.split("=")
        if (keyVal.isNotEmpty()) {
          val key = keyVal[0]
          if (!queryPairs.containsKey(key)) {
            queryPairs[key] = mutableListOf<String?>()
          }
          val value: String = if (keyVal.size > 1)
            keyVal[1]
          else ""
          queryPairs[key]?.add(value)
        }
      }
      return queryPairs
    }
  }

}
