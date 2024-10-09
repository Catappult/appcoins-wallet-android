package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class AmazonPayDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  fun consumeResult(): Array<String> {
    val result =
      arrayOf(sharedPreferences.getString(RESULT_GOOGLE_PAY, "") ?: "", sharedPreferences.getString(RESULT_CHECKOUT_SESSION_ID, "") ?: "")
    return result.also {
      saveResult("", "")
    }
  }

  fun saveResult(result: String, checkoutSessionId: String) = sharedPreferences.edit()
    .putString(RESULT_GOOGLE_PAY, result)
    .putString(RESULT_CHECKOUT_SESSION_ID, checkoutSessionId)
    .apply()

  companion object {
    internal const val RESULT_GOOGLE_PAY = "result_google_pay"
    internal const val RESULT_CHECKOUT_SESSION_ID = "result_checkout_session_id"
  }
}
