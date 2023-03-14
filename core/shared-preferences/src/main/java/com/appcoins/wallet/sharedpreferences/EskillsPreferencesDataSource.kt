package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class EskillsPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences,
) {

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
}