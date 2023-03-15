package com.appcoins.wallet.core.analytics.analytics

import android.app.Application
import android.content.Context
import com.appcoins.wallet.commons.Logger
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.appcoins.wallet.core.analytics.BuildConfig
import com.appcoins.wallet.core.analytics.analytics.logging.Log
import com.appcoins.wallet.core.utils.properties.HostProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import io.rakam.api.Rakam
import io.rakam.api.RakamClient
import io.rakam.api.TrackingOptions
import io.reactivex.Completable
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

@BoundTo(supertype = AnalyticsSetup::class)
@Named("RakamAnalytics")
class RakamAnalytics @Inject constructor(
    @ApplicationContext private val context: Context
) : AnalyticsSetup {

    private val rakamClient = Rakam.getInstance()

    companion object {
        private val TAG = RakamAnalytics::class.java.simpleName
    }

    override fun setUserId(walletAddress: String) {
        rakamClient.setUserId(walletAddress)
    }

    override fun setGamificationLevel(level: Int) {
        val superProperties = rakamClient.superProperties ?: JSONObject()
        try {
            superProperties.put("user_level", level)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        rakamClient.superProperties = superProperties
    }


    override fun setWalletOrigin(origin: String) {
        val superProperties = rakamClient.superProperties ?: JSONObject()
        try {
            superProperties.put("wallet_origin", origin)

            rakamClient.superProperties = superProperties
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        rakamClient.superProperties = superProperties
    }


    override fun setPromoCode(code: String?, bonus: Double?, validity: Int?, appName: String?) {
        val promoCode = JSONObject()
        promoCode.put("code", code)
        promoCode.put("bonus", bonus)
        promoCode.put("validity", validity)
        promoCode.put("appName", appName)
        val superProperties = rakamClient.superProperties ?: JSONObject()
        try {
            superProperties.put("promo_code", promoCode)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        rakamClient.superProperties = superProperties
    }


    fun startRakam(deviceId: String): Single<RakamClient> {
        val instance = Rakam.getInstance()
        val options = TrackingOptions()
        options.disableAdid()
        try {
            instance.initialize(
                context, URL(HostProperties.RAKAM_BASE_HOST),
                BuildConfig.RAKAM_API_KEY
            )
        } catch (e: MalformedURLException) {
            Log.e(TAG, "error: ", e)
        }
        instance.setTrackingOptions(options)
        instance.deviceId = deviceId
        instance.trackSessionEvents(true)
        instance.setLogLevel(Log.VERBOSE)
        instance.setEventUploadPeriodMillis(1)
        instance.enableLogging(true)
        return Single.just(instance)
    }

    fun setRakamSuperProperties(
        instance: RakamClient, installerPackage: String,
        userLevel: Int,
        userId: String, hasGms: Boolean, walletOrigin: String
    ) {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val superProperties = instance.superProperties ?: JSONObject()
        try {
            superProperties.put(
                AnalyticsLabels.APTOIDE_PACKAGE,
                packageInfo.versionName
            )
            superProperties.put(
                AnalyticsLabels.VERSION_CODE,
                packageInfo.versionCode
            )
            superProperties.put(
                AnalyticsLabels.ENTRY_POINT,
                if (installerPackage.isEmpty()) "other" else installerPackage
            )
            superProperties.put(AnalyticsLabels.USER_LEVEL, userLevel)
            superProperties.put(AnalyticsLabels.HAS_GMS, hasGms)
            superProperties.put(AnalyticsLabels.WALLET_ORIGIN, walletOrigin)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        instance.superProperties = superProperties
        if (userId.isNotEmpty()) instance.setUserId(userId)
        instance.enableForegroundTracking(context.applicationContext as Application)
    }

}
