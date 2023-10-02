package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class EskillsPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences,
) {
  companion object {
    private const val IS_ESKILLS_VERSION = "is_eskills_version"
    private const val ESKILLS_VERSION_CHECKED = "eskills_version_checked"
  }

  fun getTicketInQueue(walletKey: String) = sharedPreferences.getString(walletKey, null)

  fun saveTicketInQueue(
    walletKey: String,
    ticketId: String,
    dataKey: String,
    eskillsPaymentData: String
  ) =
    sharedPreferences.edit()
      .putString(walletKey, ticketId)
      .putString(dataKey, eskillsPaymentData)
      .apply()

  fun getTicketData(dataKey: String) = sharedPreferences.getString(dataKey, null)

  fun getStoredPayments(walletKey: String) = sharedPreferences.getString(walletKey, null)

  fun savePayments(walletKey: String, cachedPayment: String) =
    sharedPreferences.edit().putString(walletKey, cachedPayment).apply()

  fun isEskillsVersion(): Boolean = sharedPreferences.getBoolean(IS_ESKILLS_VERSION, false)

  fun setIsEskillsVersion(isEskillsVersion: Boolean) =
    sharedPreferences.edit().putBoolean(IS_ESKILLS_VERSION, isEskillsVersion).apply()

  fun eskillsVersionChecked(): Boolean = sharedPreferences.getBoolean(ESKILLS_VERSION_CHECKED, false)

  fun setEskillsVersionChecked(eskillsVersionChecked: Boolean) =
    sharedPreferences.edit().putBoolean(ESKILLS_VERSION_CHECKED, eskillsVersionChecked).apply()
}