package com.asfoundation.wallet.analytics

import io.rakam.api.Rakam
import org.json.JSONException
import org.json.JSONObject

class RakamAnalyticsSetup : AnalyticsSetUp {
  private val rakamClient = Rakam.getInstance()

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
}