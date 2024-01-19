package com.asfoundation.wallet.billing.googlepay.usecases

import java.net.URLEncoder
import java.util.*
import javax.inject.Inject

class BuildGooglePayUrlUseCase @Inject constructor(
) {
  operator fun invoke(
    url: String,
    sessionId: String,
    sessionData: String,
    price: String,
    currency: String
  ): String {
    val encodedSessionData = URLEncoder.encode(sessionData, "UTF-8")
    val currenySymbol = URLEncoder.encode(Currency.getInstance(currency).symbol, "UTF-8")
    return "$url?id=$sessionId&sessionData=$encodedSessionData&price=$price&currency=$currenySymbol"
  }
}
