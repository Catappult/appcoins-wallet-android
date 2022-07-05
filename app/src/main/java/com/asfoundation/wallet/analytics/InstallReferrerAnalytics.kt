package com.asfoundation.wallet.analytics

import android.content.Context
import cm.aptoide.analytics.AnalyticsManager
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingFromIapPackageNameUseCase
import com.asfoundation.wallet.onboarding.use_cases.SetOnboardingFromIapStateUseCase
import com.asfoundation.wallet.util.Log
import com.asfoundation.wallet.util.UrlUtmParser
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URLDecoder
import java.util.*
import javax.inject.Inject

class InstallReferrerAnalytics @Inject constructor(
  @ApplicationContext val context: Context,
  private val analyticsManager: AnalyticsManager,
  private val setOnboardingFromIapStateUseCase: SetOnboardingFromIapStateUseCase,
  private val setOnboardingFromIapPackageNameUseCase: SetOnboardingFromIapPackageNameUseCase
) {

  private lateinit var referrerClient: InstallReferrerClient

  fun sendFirstInstallInfo(sendEvent: Boolean = false) {
    referrerClient = InstallReferrerClient.newBuilder(context).build()
    referrerClient.startConnection(object : InstallReferrerStateListener {
      val firstLaunchEmptyData: MutableMap<String, Any> = HashMap()
      override fun onInstallReferrerSetupFinished(responseCode: Int) {
        when (responseCode) {
          InstallReferrerClient.InstallReferrerResponse.OK -> {
            // Connection established.
            val response: ReferrerDetails = referrerClient.installReferrer
            val referrerUrl: String = response.installReferrer
            val referrerClickTime: Long = response.referrerClickTimestampSeconds
            val appInstallTime: Long = response.installBeginTimestampSeconds

            val referrerData = getReferrerData(referrerUrl, sendEvent)
            if (sendEvent) sendFirstLaunchEvent(referrerData)
          }
          InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
            Log.d("Referrer", "not supported")
            if (sendEvent) sendFirstLaunchEvent(firstLaunchEmptyData)
            setOnboardingFromIapStateUseCase(false)
          }
          InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
            Log.d("Referrer", "unavailable")
            if (sendEvent) sendFirstLaunchEvent(firstLaunchEmptyData)
            setOnboardingFromIapStateUseCase(false)
          }
        }
      }

      override fun onInstallReferrerServiceDisconnected() {

      }
    })
  }

  private fun getReferrerData(referrerUrl: String, sendEvent: Boolean) : MutableMap<String, Any> {
    Log.d("Referrer", referrerUrl)

    val decodedReferrer = URLDecoder.decode(referrerUrl, "UTF-8")
    val urlParams = UrlUtmParser.splitQuery(decodedReferrer)

    val firstLaunchData: MutableMap<String, Any> = HashMap()
    if (isIntegrationFlowIAB(referrerUrl)) {
      firstLaunchData[PACKAGE_NAME] = urlParams[UTM_MEDIUM]?.get(0) ?: ""
      firstLaunchData[INTEGRATION_FLOW] = urlParams[UTM_TERM]?.get(0) ?: ""
      firstLaunchData[SOURCE] = urlParams[UTM_SOURCE]?.get(0) ?: ""
      firstLaunchData[SKU] = urlParams[UTM_CONTENT]?.get(0) ?: ""
    } else { // when the installation is not from an iab, this fields are not meaningful
      firstLaunchData[PACKAGE_NAME] = ""
      firstLaunchData[INTEGRATION_FLOW] = ""
      firstLaunchData[SOURCE] = ""
      firstLaunchData[SKU] = ""
    }
    val appPackageName = firstLaunchData[PACKAGE_NAME].toString()
    if (appPackageName.isNotBlank() && !sendEvent) {
      setOnboardingFromIapStateUseCase(true)
      setOnboardingFromIapPackageNameUseCase(appPackageName)
    }
    referrerClient.endConnection()
    return firstLaunchData
  }

  private fun sendFirstLaunchEvent(firstLaunchData : MutableMap<String, Any>) {
    analyticsManager.logEvent(
      firstLaunchData,
      FIRST_LAUNCH,
      AnalyticsManager.Action.OPEN,
      WALLET
    )
  }

  /**
   * Checks if the first launch of the app was made from an IAB (either from sdk or osp)
   * Only osp are considered for now
   */
  private fun isIntegrationFlowIAB(referrerUrl: String): Boolean {
    val decodedReferrer = URLDecoder.decode(referrerUrl, "UTF-8")
    val urlParams: Map<String, MutableList<String?>> = UrlUtmParser.splitQuery(decodedReferrer)
    val integrationFlow = (urlParams[UTM_TERM]?.get(0) ?: "").toLowerCase(Locale.ENGLISH)
    return (integrationFlow == "sdk" || integrationFlow == "osp")
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
