package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class PayPalDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  fun consumeResult(): String {
    val result = sharedPreferences.getString(RESULT_PAY_PAL, "") ?: ""
    return result.also {
      saveResult("")
    }
  }

  fun saveResult(result: String) = sharedPreferences.edit()
    .putString(RESULT_PAY_PAL, result)
    .apply()

  companion object {
    internal const val RESULT_PAY_PAL = "result_pay_pal"
  }
}
