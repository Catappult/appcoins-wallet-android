package com.asfoundation.wallet.util

import android.net.Uri
import com.asf.wallet.BuildConfig

class Parameters {
  companion object {
    const val VALUE = "value"
    const val TO = "to"
    const val PRODUCT = "product"
    const val DOMAIN = "domain"
    const val DATA = "data"
    const val CURRENCY = "currency"
    const val CALLBACK_URL = "callback_url"
    const val SCHEME = "https"
    const val HOST = BuildConfig.PAYMENT_HOST
    const val SECOND_HOST = BuildConfig.SECOND_PAYMENT_HOST
    const val PATH = "/transaction"
    const val PAYMENT_TYPE_INAPP_UNMANAGED = "inapp_unmanaged"
    const val NETWORK_ID_ROPSTEN = 3L
    const val NETWORK_ID_MAIN = 1L
  }
}

fun Uri.isOneStepURLString() =
    scheme == Parameters.SCHEME && (host == Parameters.HOST || host == Parameters.SECOND_HOST)
        && path.startsWith(Parameters.PATH)

fun parseOneStep(uri: Uri): OneStepUri {
  val scheme = uri.scheme
  val host = uri.host
  val path = uri.path
  val parameters = mutableMapOf<String, String>()
  parameters.apply {
    for (key in uri.queryParameterNames) {
      this[key] = uri.getQueryParameter(key)
    }
  }
  return OneStepUri(scheme, host, path, parameters)
}

