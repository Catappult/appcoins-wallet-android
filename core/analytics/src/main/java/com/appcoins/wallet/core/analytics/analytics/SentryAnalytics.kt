package com.appcoins.wallet.core.analytics.analytics

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryItemType
import io.sentry.SentryLevel
import io.sentry.protocol.User
import org.json.JSONObject

class SentryAnalytics : AnalyticsSetup {

    override fun setUserId(walletAddress: String) {
        val old = User()
        val user = User().apply {
            id = walletAddress
        }
        Sentry.addBreadcrumb(
            Breadcrumb("Changing wallet from $old to $walletAddress").apply {
                type = SentryItemType.UserFeedback.itemType
                level = SentryLevel.INFO
                category = "wallet"
            }
        )
        Sentry.setUser(user)
    }

    override fun setGamificationLevel(level: Int) {
        Sentry.setExtra(AnalyticsLabels.USER_LEVEL, level.toString())
    }

    override fun setWalletOrigin(origin: String) {
        Sentry.setExtra(AnalyticsLabels.WALLET_ORIGIN, origin)
    }

    override fun setPromoCode(code: String?, bonus: Double?, validity: Int?, appName: String?) {
        val promoCode = JSONObject()
        promoCode.put("code", code)
        promoCode.put("bonus", bonus)
        promoCode.put("validity", validity)
        promoCode.put("appName", appName)
        Sentry.setExtra(AnalyticsLabels.PROMO_CODE, promoCode.toString())
    }
}
