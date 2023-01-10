package com.asfoundation.wallet.billing.gpay

import android.app.Activity
import com.asf.wallet.BuildConfig
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Contains helper static methods for dealing with the Payments API.
 *
 * Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
object GooglePayUtils {

  val CENTS = BigDecimal(100)

  /**
   * Create a Google Pay API base request object with properties used in all requests.
   *
   * @return Google Pay API base request object.
   * @throws JSONException
   */
  private val baseRequest = JSONObject().apply {
    put("apiVersion", 2)
    put("apiVersionMinor", 0)
  }

  /**
   * Gateway Integration: Identify your gateway and your app's gateway merchant identifier.
   *
   *
   * The Google Pay API response will return an encrypted payment method capable of being charged
   * by a supported gateway after payer authorization.
   *
   *
   * TODO: Check with your gateway on the parameters to pass and modify them in Constants.java.
   *
   * @return Payment data tokenization for the CARD payment method.
   * @throws JSONException
   * @see [PaymentMethodTokenizationSpecification](https://developers.google.com/pay/api/android/reference/object.PaymentMethodTokenizationSpecification)
   */
  private fun gatewayTokenizationSpecification(): JSONObject {
    return JSONObject().apply {
      put("type", "PAYMENT_GATEWAY")
      put("parameters", JSONObject(mapOf(
        "gateway" to "adyen",
        "gatewayMerchantId" to "AptoideUSD")))    //TODO
    }
  }

//  /**
//   * `DIRECT` Integration: Decrypt a response directly on your servers. This configuration has
//   * additional data security requirements from Google and additional PCI DSS compliance complexity.
//   *
//   *
//   * Please refer to the documentation for more information about `DIRECT` integration. The
//   * type of integration you use depends on your payment processor.
//   *
//   * @return Payment data tokenization for the CARD payment method.
//   * @throws JSONException
//   * @see [PaymentMethodTokenizationSpecification](https://developers.google.com/pay/api/android/reference/object.PaymentMethodTokenizationSpecification)
//   */
//  private fun directTokenizationSpecification(): JSONObject {
//    if (Constants.DIRECT_TOKENIZATION_PUBLIC_KEY == "REPLACE_ME" ||
//      Constants.DIRECT_TOKENIZATION_PARAMETERS.isEmpty() ||
//      Constants.DIRECT_TOKENIZATION_PUBLIC_KEY.isEmpty()
//    ) {
//
//      throw RuntimeException(
//        "Please edit the Constants.java file to add protocol version & public key."
//      )
//    }
//
//    return JSONObject().apply {
//      put("type", "DIRECT")
//      put("parameters", JSONObject(Constants.DIRECT_TOKENIZATION_PARAMETERS))
//    }
//  }

  /**
   * Card networks supported by your app and your gateway.
   *
   * @return Allowed card networks
   * @see [CardParameters](https://developers.google.com/pay/api/android/reference/object.CardParameters)
   */
  private val allowedCardNetworks = JSONArray(listOf(
    "AMEX",
    "DISCOVER",
//    "INTERAC",
    "MAESTRO",
//    "JCB",
    "MASTERCARD",
    "VISA"
  ))

  /**
   * Card authentication methods supported by your app and your gateway.
   *
   *
   *
   * @return Allowed card authentication methods.
   * @see [CardParameters](https://developers.google.com/pay/api/android/reference/object.CardParameters)
   */
  private val allowedCardAuthMethods = JSONArray(listOf(
    "PAN_ONLY",
    "CRYPTOGRAM_3DS"
  ))

  /**
   * Describe your app's support for the CARD payment method.
   *
   *
   * The provided properties are applicable to both an IsReadyToPayRequest and a
   * PaymentDataRequest.
   *
   * @return A CARD PaymentMethod object describing accepted cards.
   * @throws JSONException
   * @see [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
   */
  // Optionally, you can add billing address/phone number associated with a CARD payment method.
  private fun baseCardPaymentMethod(): JSONObject {
    return JSONObject().apply {

      val parameters = JSONObject().apply {
        put("allowedAuthMethods", allowedCardAuthMethods)
        put("allowedCardNetworks", allowedCardNetworks)
        put("billingAddressRequired", true)    //TODO confirm
        put("billingAddressParameters", JSONObject().apply {
          put("format", "FULL")
        })
      }

      put("type", "CARD")
      put("parameters", parameters)
    }
  }

  /**
   * Describe the expected returned payment data for the CARD payment method
   *
   * @return A CARD PaymentMethod describing accepted cards and optional fields.
   * @throws JSONException
   * @see [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
   */
  private fun cardPaymentMethod(): JSONObject {
    val cardPaymentMethod = baseCardPaymentMethod()
    cardPaymentMethod.put("tokenizationSpecification", gatewayTokenizationSpecification())

    return cardPaymentMethod
  }

  /**
   * An object describing accepted forms of payment by your app, used to determine a viewer's
   * readiness to pay.
   *
   * @return API version and payment methods supported by the app.
   * @see [IsReadyToPayRequest](https://developers.google.com/pay/api/android/reference/object.IsReadyToPayRequest)
   */
  val isReadyToPayRequest: JSONObject?
    get() = try {
      baseRequest.apply {
        put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
      }
    } catch (e: JSONException) {
      null
    }

  /**
   * Information about the merchant requesting payment information
   *
   * @return Information about the merchant.
   * @throws JSONException
   * @see [MerchantInfo](https://developers.google.com/pay/api/android/reference/object.MerchantInfo)
   */
  private val merchantInfo: JSONObject =
    JSONObject().put("merchantName", "Aptoide")  //TODO check

  /**
   * Creates an instance of [PaymentsClient] for use in an [Activity] using the
   * environment and theme set in [Constants].
   *
   * @param activity is the caller's activity.
   */
  fun createPaymentsClient(activity: Activity): PaymentsClient {
    val walletOptions = Wallet.WalletOptions.Builder()
      .setEnvironment(if(BuildConfig.DEBUG)
        WalletConstants.ENVIRONMENT_TEST
      else
        WalletConstants.ENVIRONMENT_PRODUCTION
      )
      .build()

    return Wallet.getPaymentsClient(activity, walletOptions)
  }

  /**
   * Provide Google Pay API with a payment amount, currency, and amount status.
   *
   * @return information about the requested payment.
   * @throws JSONException
   * @see [TransactionInfo](https://developers.google.com/pay/api/android/reference/object.TransactionInfo)
   */
  private fun getTransactionInfo(price: String): JSONObject {
    return JSONObject().apply {
      put("totalPrice", price)
      put("totalPriceStatus", "FINAL")
//      put("countryCode", Constants.COUNTRY_CODE)
//      put("currencyCode", Constants.CURRENCY_CODE)
    }
  }

  /**
   * An object describing information requested in a Google Pay payment sheet
   *
   * @return Payment data expected by your app.
   * @see [PaymentDataRequest](https://developers.google.com/pay/api/android/reference/object.PaymentDataRequest)
   */
  fun getPaymentDataRequest(priceCents: Long): JSONObject? {
    return try {
      baseRequest.apply {
        put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod()))
        put("transactionInfo", getTransactionInfo(priceCents.centsToString()))
        put("merchantInfo", merchantInfo)

        // An optional shipping address requirement is a top-level property of the
        // PaymentDataRequest JSON object.
        val shippingAddressParameters = JSONObject().apply {
          put("phoneNumberRequired", false)
          put("allowedCountryCodes", JSONArray(listOf("US", "GB")))
        }
        put("shippingAddressParameters", shippingAddressParameters)
        put("shippingAddressRequired", true)
      }
    } catch (e: JSONException) {
      null
    }
  }
}

/**
 * Converts cents to a string format accepted by [PaymentsUtil.getPaymentDataRequest].
 *
 * @param cents value of the price.
 */
fun Long.centsToString() = BigDecimal(this)
  .divide(GooglePayUtils.CENTS)
  .setScale(2, RoundingMode.HALF_EVEN)
  .toString()

