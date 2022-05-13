package com.asfoundation.wallet.analytics

import android.content.Context
import cm.aptoide.analytics.AnalyticsManager
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.asfoundation.wallet.util.UrlUtmParser
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URLDecoder
import java.util.*
import javax.inject.Inject

class InstallReferrerAnalytics @Inject constructor(
  @ApplicationContext val context: Context,
  private val analyticsManager: AnalyticsManager
) {

  private lateinit var referrerClient: InstallReferrerClient

  fun sendFirstInstallInfo() {
    referrerClient = InstallReferrerClient.newBuilder(context).build()
    referrerClient.startConnection(object : InstallReferrerStateListener {

      override fun onInstallReferrerSetupFinished(responseCode: Int) {
        when (responseCode) {
          InstallReferrerClient.InstallReferrerResponse.OK -> {
            // Connection established.
            val response: ReferrerDetails = referrerClient.installReferrer
            val referrerUrl: String = response.installReferrer
            val referrerClickTime: Long = response.referrerClickTimestampSeconds
            val appInstallTime: Long = response.installBeginTimestampSeconds
            val instantExperienceLaunched: Boolean = response.googlePlayInstantParam

            sendFirstLaunchEvent(referrerUrl)

          }
          InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
            //TODO not googlePlay
          }
          InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
            //TODO not googlePlay
          }
        }
      }
      override fun onInstallReferrerServiceDisconnected() {

      }
    })
  }

  private fun sendFirstLaunchEvent(referrerUrl: String) {

    val decodedReferrer = URLDecoder.decode(referrerUrl, "UTF-8")
    val urlParams = UrlUtmParser.splitQuery(decodedReferrer)

    val firstLaunchData: MutableMap<String, Any> = HashMap()
    firstLaunchData[PACKAGE_NAME] = urlParams?.get(UTM_MEDIUM)?.get(0) ?: ""
    firstLaunchData[INTEGRATION_FLOW] = urlParams?.get(UTM_TERM)?.get(0) ?: ""
    firstLaunchData[SOURCE] = urlParams?.get(UTM_SOURCE)?.get(0) ?: ""
    firstLaunchData[SKU] = urlParams?.get(UTM_CONTENT)?.get(0) ?: ""

    analyticsManager.logEvent(
      firstLaunchData,
      FIRST_LAUNCH,
      AnalyticsManager.Action.OPEN,
      WALLET
    )

  }

  companion object {
    const val WALLET = "WALLET"
    const val FIRST_LAUNCH = "wallet_first_launch"
    const val PACKAGE_NAME = "package_name"
    const val INTEGRATION_FLOW = "integration_flow"
    const val SOURCE = "source"
    const val SKU = "sku"

    const val UTM_SOURCE = "utm_source"
    const val UTM_MEDIUM = "utm_medium"
    const val UTM_TERM = "utm_term"
    const val UTM_CONTENT = "utm_content"
  }

}