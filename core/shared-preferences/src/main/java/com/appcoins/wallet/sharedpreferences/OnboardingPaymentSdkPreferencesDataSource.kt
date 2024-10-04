package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject


class OnboardingPaymentSdkPreferencesDataSource @Inject constructor(private val sharedPreferences: SharedPreferences) {

  private companion object {
    private const val WS_PORT = "ws_port"
    private const val SDK_VERSION = "sdk_version"
    private const val RESPONSE_CODE_WEB_SOCKET = "response_code_web_socket"
  }

  fun setWsPort(wsPort: String) =
    sharedPreferences.edit()
      .putString(WS_PORT, wsPort)
      .apply()

  fun getWsPort() = sharedPreferences.getString(WS_PORT, "")

  fun setSdkVersion(sdkVersion: String) =
    sharedPreferences.edit()
      .putString(SDK_VERSION, sdkVersion)
      .apply()

  fun getSdkVersion() = sharedPreferences.getString(SDK_VERSION, "")

  fun setResponseCodeWebSocket(responseCode: Int) =
    sharedPreferences.edit()
      .putInt(RESPONSE_CODE_WEB_SOCKET, responseCode)
      .apply()

  fun getResponseCodeWebSocket() = sharedPreferences.getInt(RESPONSE_CODE_WEB_SOCKET, 1)
}