package com.appcoins.wallet.core.analytics.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import cm.aptoide.analytics.AnalyticsManager
import cm.aptoide.analytics.EventLogger
import com.appcoins.wallet.sharedpreferences.AppStartPreferencesDataSource
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GAEventLogger @Inject constructor(
  private val indicativeAnalytics: IndicativeAnalytics,
  private val appStartPreferencesDataSource: AppStartPreferencesDataSource,
  @ApplicationContext private val context: Context
) : EventLogger {

  private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

  companion object {
    private const val TAG = "GAEventLogger"
    private const val FIRST_PAYMENT = "FIRST_PAYMENT"
    private const val REGULAR_PAYMENT = "REGULAR_PAYMENT"
  }

  override fun setup() = Unit

  override fun log(
    eventName: String,
    data: Map<String, Any>?,
    action: AnalyticsManager.Action,
    context: String
  ) {
    val completedData: Map<String, Any> = (data ?: emptyMap()) + mapOf(
      AnalyticsLabels.DEVICE_ORIENTATION to indicativeAnalytics.findDeviceOrientation(),
      AnalyticsLabels.PAYMENT_FUNNEL to if (appStartPreferencesDataSource.getIsFirstPayment())
        FIRST_PAYMENT else REGULAR_PAYMENT
    )

    if (indicativeAnalytics.usrId.isNotEmpty())
      firebaseAnalytics.setUserId(indicativeAnalytics.usrId)

    indicativeAnalytics.superProperties.forEach { (key, value) ->
      firebaseAnalytics.setUserProperty(key, value.toString())
    }

    val paramsBundle = Bundle().apply {
      completedData.forEach { (key, value) -> putAny(key, value) }
    }

    firebaseAnalytics.logEvent(eventName, paramsBundle)

    Log.d(
      TAG,
      "logGA() → eventName=$eventName, action=$action, context=$context, " +
          "completedData=$completedData, userId=${indicativeAnalytics.usrId}"
    )
  }

  private fun Bundle.putAny(key: String, value: Any?) {
    when (value) {
      null -> return
      is String -> putString(key, value)
      is Int -> putInt(key, value)
      is Long -> putLong(key, value)
      is Double -> putDouble(key, value)
      is Float -> putFloat(key, value)
      is Boolean -> putBoolean(key, value)
      else -> putString(key, value.toString())
    }
  }
}