package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class GooglePayDataSource @Inject constructor(private val sharedPreferences: SharedPreferences) {
  fun consumeResult(): String {
    val result = sharedPreferences.getString(RESULT_GOOGLE_PAY, "") ?: ""
    return result.also { saveResult("") }
  }

  fun saveResult(result: String) =
      sharedPreferences.edit().putString(RESULT_GOOGLE_PAY, result).apply()

  companion object {
    internal const val RESULT_GOOGLE_PAY = "result_google_pay"
  }
}
