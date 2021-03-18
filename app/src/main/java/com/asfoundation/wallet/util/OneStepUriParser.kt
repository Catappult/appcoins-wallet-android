package com.asfoundation.wallet.util

import android.net.Uri
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.util.Parameters.Companion.CALLBACK_URL
import com.asfoundation.wallet.util.Parameters.Companion.CURRENCY
import com.asfoundation.wallet.util.Parameters.Companion.DATA
import com.asfoundation.wallet.util.Parameters.Companion.DOMAIN
import com.asfoundation.wallet.util.Parameters.Companion.HOST
import com.asfoundation.wallet.util.Parameters.Companion.LEGACY_HOST
import com.asfoundation.wallet.util.Parameters.Companion.PATH
import com.asfoundation.wallet.util.Parameters.Companion.PAYMENT_TYPE_INAPP
import com.asfoundation.wallet.util.Parameters.Companion.PAYMENT_TYPE_INAPP_UNMANAGED
import com.asfoundation.wallet.util.Parameters.Companion.PRODUCT
import com.asfoundation.wallet.util.Parameters.Companion.SCHEME
import com.asfoundation.wallet.util.Parameters.Companion.VALUE
import java.util.*

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
    const val LEGACY_HOST = BuildConfig.LEGACY_PAYMENT_HOST
    const val HOST = BuildConfig.PAYMENT_HOST
    const val PATH = "/transaction"
    const val PAYMENT_TYPE_INAPP_UNMANAGED = "INAPP_UNMANAGED"
    const val PAYMENT_TYPE_INAPP = "INAPP"
    const val PAYMENT_TYPE_VOUCHER = "VOUCHER"
    const val PAYMENT_TYPE_DONATION = "DONATION"
    const val NETWORK_ID_ROPSTEN = 3L
    const val NETWORK_ID_MAIN = 1L
  }
}

fun Uri.isOneStepURLString() =
    scheme == Parameters.SCHEME && (host == HOST || host == LEGACY_HOST)
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

/**
 * Creates a one-step URI from a TransactionBuilder
 *
 * Example URI string
 * https://apichain-dev.blockchainds.com/transaction/inapp?product=oil&value=0.05&currency=USD&domain=com.appcoins.trivialdrivesample.test
 *
 * It is an extension function and not a member function since it's not a direct responsibility
 * of TransactionBuilder
 */
fun TransactionBuilder.toOneStepUri(): Uri {
  val transactionType =
      if (type.equals(PAYMENT_TYPE_INAPP_UNMANAGED, ignoreCase = true)) PAYMENT_TYPE_INAPP else type

  val parameters = mutableListOf<Parameter>()
  skuId?.let { parameters.add(Parameter(PRODUCT, skuId)) }
  amount()?.let { parameters.add(Parameter(VALUE, amount().toString())) }
  fiatCurrency?.let { parameters.add(Parameter(CURRENCY, fiatCurrency)) }
  domain?.let { parameters.add(Parameter(DOMAIN, domain)) }
  payload?.let { parameters.add(Parameter(DATA, payload)) }
  callbackUrl?.let { parameters.add(Parameter(CALLBACK_URL, callbackUrl)) }

  val uriString = "$SCHEME://${BuildConfig.PAYMENT_HOST}$PATH/" +
      "${transactionType.toLowerCase(Locale.getDefault())}?${getParametersString(parameters)}"
  return Uri.parse(uriString)
}

private data class Parameter(val name: String, val value: String)

private fun getParametersString(parameters: List<Parameter>): String {
  var string = ""
  parameters.onEach { param -> string = string.plus("${param.name}=${param.value}&") }
  if (string.isNotEmpty()) string = string.removeSuffix("&")
  return string
}

