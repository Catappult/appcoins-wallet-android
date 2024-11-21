package com.appcoins.wallet.core.analytics.analytics

import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.protocol.User
import org.json.JSONObject
import javax.inject.Inject

class SentryAnalytics @Inject constructor() : AnalyticsSetup {

  override fun setUserId(walletAddress: String) {
    var oldUserId = "unknown"
    Sentry.withScope { scope ->
      oldUserId = scope.user?.id ?: "unknown"

      scope.setTag("category", "wallet")
      scope.level = SentryLevel.INFO

      val user = User().apply {
        id = walletAddress
      }
      scope.user = user
    }

    // Capture the message AFTER setting the new user and outside the withScope block
    Sentry.captureMessage("Changing wallet from $oldUserId to $walletAddress")
  }

  override fun setGamificationLevel(level: Int) {
    Sentry.withScope { scope ->
      scope.setTag(AnalyticsLabels.USER_LEVEL, level.toString())
    }
  }

  override fun setWalletOrigin(origin: String) {
    Sentry.withScope { scope ->
      scope.setTag(AnalyticsLabels.WALLET_ORIGIN, origin)
    }
  }

  override fun setPromoCode(code: String?, bonus: Double?, validity: Int?, appName: String?) {
    val promoCode = JSONObject()
    promoCode.put("code", code)
    promoCode.put("bonus", bonus)
    promoCode.put("validity", validity)
    promoCode.put("appName", appName)
    Sentry.withScope { scope ->
      scope.setTag(AnalyticsLabels.PROMO_CODE, promoCode.toString())
    }
  }
}
