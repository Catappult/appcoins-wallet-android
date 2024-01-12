package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class GooglePayDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  fun getResult(): String = sharedPreferences.getString(RESULT_GOOGLE_PAY, "") ?: ""

  fun saveResult(result: String) = sharedPreferences.edit()
    .putString(RESULT_GOOGLE_PAY, result)
    .apply()

  companion object {
    internal const val RESULT_GOOGLE_PAY = "result_google_pay"
  }
}
