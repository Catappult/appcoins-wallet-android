package com.appcoins.wallet.core.analytics.analytics

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import io.sentry.event.Breadcrumb
import io.sentry.event.BreadcrumbBuilder
import io.sentry.event.User
import org.json.JSONObject
import javax.inject.Inject

class SentryAnalytics @Inject constructor(
    @ApplicationContext private val context: Context
) :
    AnalyticsSetup {

    override fun setUserId(walletAddress: String) {
        val old = Sentry.getContext().user.id
        Sentry.getContext().recordBreadcrumb(
            BreadcrumbBuilder()
                .setType(Breadcrumb.Type.USER)
                .setLevel(Breadcrumb.Level.INFO)
                .setMessage("Changing wallet from $old to $walletAddress")
                .setCategory("wallet")
                .build()
        )
        Sentry.getContext().user = User(walletAddress, null, null, null)
    }

    override fun setGamificationLevel(level: Int) {
        Sentry.getContext().addExtra(AnalyticsLabels.USER_LEVEL, level)
    }

    override fun setWalletOrigin(origin: String) {
        Sentry.getContext().addExtra(AnalyticsLabels.WALLET_ORIGIN, origin)
    }

    override fun setPromoCode(code: String?, bonus: Double?, validity: Int?, appName: String?) {
        val promoCode = JSONObject()
        promoCode.put("code", code)
        promoCode.put("bonus", bonus)
        promoCode.put("validity", validity)
        promoCode.put("appName", appName)
        Sentry.getContext().addExtra(AnalyticsLabels.PROMO_CODE, promoCode)
    }
}
