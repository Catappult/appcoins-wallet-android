package com.asfoundation.wallet.billing.paypal

data class PaypalV2CreateTokenResponse(
  val token: String,
  val redirect: Redirect
) {
  data class Redirect(
    val url: String,
    val method: String
  )
}
