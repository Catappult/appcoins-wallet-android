package com.asfoundation.wallet.billing.googlepay.usecases

import java.net.URLEncoder
import javax.inject.Inject

class BuildGooglePayUrlUseCase @Inject constructor(
) {
  operator fun invoke(
    url: String,
    sessionId: String,
    sessionData: String,
    isDarkMode: Boolean,
  ): String {
    val encodedSessionData = URLEncoder.encode(sessionData, "UTF-8")
    return "$url?id=$sessionId&sessionData=$encodedSessionData&isDarkMode=$isDarkMode"
  }
}
