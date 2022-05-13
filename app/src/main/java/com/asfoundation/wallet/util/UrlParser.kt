package com.asfoundation.wallet.util

import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class UrlUtmParser {

  companion object {

    @Throws(UnsupportedEncodingException::class)
    fun splitQuery(urlParams: String): Map<String, MutableList<String?>>? {
      val queryPairs: MutableMap<String, MutableList<String?>> = LinkedHashMap()
      val pairs: List<String> = urlParams.split("&")
      for (pair in pairs) {
        val idx = pair.indexOf("=")
        val key = if (idx > 0) URLDecoder.decode(pair.substring(0, idx), "UTF-8") else pair
        if (!queryPairs.containsKey(key)) {
          queryPairs[key] = mutableListOf<String?>()
        }
        val value: String? = if (idx > 0 && pair.length > idx + 1)
          URLDecoder.decode(
            pair.substring(idx + 1),
            "UTF-8"
          ) else null
        queryPairs[key]?.add(value)
      }
      return queryPairs
    }
  }

}