package com.asfoundation.wallet.billing.amazonPay.models

import java.util.Locale

class AmazonConst {
  companion object {
    var APP_LINK_HOST = mapOf(
      "US" to "www.amazon.com",
      "DE" to "www.amazon.de",
      "UK" to "www.amazon.co.uk",
      "FR" to "www.amazon.fr",
      "IT" to "www.amazon.it",
      "ES" to "www.amazon.es",
      "JP" to "www.amazon.co.jp"
    )
    var APP_LINK_PATH = mapOf(
      "US" to "/apay/checkout/initiate/NA",
      "DE" to "/apay/checkout/initiate/DE",
      "UK" to "/apay/checkout/initiate/UK",
      "FR" to "/apay/checkout/initiate/FR",
      "IT" to "/apay/checkout/initiate/IT",
      "ES" to "/apay/checkout/initiate/ES",
      "JP" to "/apay/checkout/initiate/JP"
    )

   var CHECKOUT_LANGUAGE = mapOf(
      "US" to "en_US",
      "DE" to "de_DE",
      "UK" to "en_GB",
      "FR" to "fr_FR",
      "IT" to "it_IT",
      "ES" to "es_ES",
      "JP" to "ja_JP"
   )

    fun getUserCheckoutLanguage(): String {
      val userCountry = Locale.getDefault().country
      return CHECKOUT_LANGUAGE.getOrDefault(userCountry, "en_US")
    }
  }
}