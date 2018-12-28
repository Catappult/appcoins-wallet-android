package com.appcoins.wallet.billing.util

import android.net.Uri
import com.appcoins.billing.AppcoinsBilling

/**
 * Intent payload helper class that provide a way to send the developers wallet address together
 * with an already existent developers payload when using [AppcoinsBilling.getBuyIntent].
 *
 * The use of this helper is mandatory even if there is no  existing payload, because it allows for
 * a payment to be delivered to the developers ethereum address.
 *
 * This class must be imported to your project and used without any changes to be compatible with
 * the Appcoins billing process.
 */
object PayloadHelper {
  private const val SCHEME = "appcoins"
  private const val ADDRESS_PARAMETER = "address"
  private const val ORDER_PARAMETER = "order_reference"
  private const val PAYLOAD_PARAMETER = "payload"

  /**
   * Method to build the payload required on the [AppcoinsBilling.getBuyIntent] method.
   * @param developerAddress The developer's ethereum address
   * @param developerPayload The additional payload to be sent
   *
   * @return The final developers payload to be sent
   */
  fun buildIntentPayload(developerAddress: String? = null, developerPayload: String? = null,
                         orderReference: String? = null): String {
    val builder = Uri.Builder()
    builder.scheme(SCHEME)
        .authority("appcoins.io")
    developerAddress?.let { builder.appendQueryParameter(ADDRESS_PARAMETER, it) }
    developerPayload?.let { builder.appendQueryParameter(PAYLOAD_PARAMETER, it) }
    orderReference?.let { builder.appendQueryParameter(ORDER_PARAMETER, it) }
    return builder.toString()
  }

  /**
   * Given a uri string validate if it is part of the expected scheme and if so return the
   * developer's ethereum address.
   *
   * @param uriString The payload uri content
   *
   * @return The developers ethereum address
   */
  fun getAddress(uriString: String): String {
    val uri = Uri.parse(uriString)
    return if (uri.scheme.equals(SCHEME, ignoreCase = true)) {
      uri.getQueryParameter(ADDRESS_PARAMETER)
    } else {
      throw IllegalArgumentException()
    }
  }

  fun getOrderReference(uriString: String): String? {
    val uri = Uri.parse(uriString)
    return if (uri.scheme.equals(SCHEME, ignoreCase = true)) {
      uri.getQueryParameter(ORDER_PARAMETER)
    } else {
      throw IllegalArgumentException()
    }
  }

  /**
   * Given a uri string validate if it is part of the expected scheme and if so return the
   * addition payload content.
   *
   * @param uriString The payload uri content
   *
   * @return The additional payload content
   */
  fun getPayload(uriString: String?): String? {
    return uriString?.let {
      val uri = Uri.parse(it)
      if (uri.scheme.equals(SCHEME, ignoreCase = true)) {
        uri.getQueryParameter(PAYLOAD_PARAMETER)
      } else {
        throw IllegalArgumentException()
      }
    }
  }
}
