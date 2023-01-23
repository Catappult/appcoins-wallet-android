package com.asfoundation.wallet.util

import android.net.Uri
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.util.Parameters.Companion.HOST
import com.asfoundation.wallet.util.Parameters.Companion.LEGACY_HOST

class Parameters {
  companion object {
    const val VALUE = "value"
    const val TO = "to"
    const val PRODUCT = "product"
    const val DOMAIN = "domain"
    const val DATA = "data"
    const val CURRENCY = "currency"
    const val TYPE = "type"
    const val CALLBACK_URL = "callback_url"
    const val ORDER_REFERENCE = "order_reference"
    const val PRODUCT_TOKEN = "product_token"
    const val SKILLS = "skills"
    const val SCHEME = "https"
    const val LEGACY_HOST = BuildConfig.LEGACY_PAYMENT_HOST
    const val HOST = BuildConfig.PAYMENT_HOST
    const val PATH = "/transaction"
    const val PAYMENT_TYPE_INAPP_UNMANAGED = "INAPP_UNMANAGED"
    const val ESKILLS = "ESKILLS"
    const val NETWORK_ID_ROPSTEN = 3L
    const val NETWORK_ID_MAIN = 1L
  }
}

fun Uri.isOneStepURLString() =
  scheme == Parameters.SCHEME && (host == HOST || host == LEGACY_HOST)
      && (path?.startsWith(Parameters.PATH) ?: false)

fun parseOneStep(uri: Uri): OneStepUri {
  val scheme = uri.scheme
  val host = uri.host
  val path = uri.path
  val parameters = mutableMapOf<String, String>()
  parameters.apply {
    for (key in uri.queryParameterNames) {
      uri.getQueryParameter(key)
        ?.let { parameter ->
          this[key] = parameter
        }
    }
  }
  return OneStepUri(scheme ?: "", host ?: "", path ?: "", parameters)
}
