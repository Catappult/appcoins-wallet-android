package com.asfoundation.wallet.billing.paypal

data class PaypalCreateToken(
  val token: String,
  val redirect: Redirect
) {

  data class Redirect(
    val url: String,
    val method: String
  )

  companion object {
    fun map(response: PaypalV2CreateTokenResponse): PaypalCreateToken {
      return PaypalCreateToken(
        response.token,
        Redirect(
          response.redirect.url,
          response.redirect.method
        )
      )
    }
  }

}
