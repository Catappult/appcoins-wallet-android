package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import com.appcoins.wallet.sharedpreferences.GooglePayDataSource.Companion.RESULT_GOOGLE_PAY
import javax.inject.Inject

class AmazonPayDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  fun consumeResult(): String {
    val result = sharedPreferences.getString(RESULT_CHECKOUT_SESSION_ID, "") ?: ""
    return result.also {
      saveResult("")
    }
  }

  fun saveResult(amazonCheckoutSessionId: String) = sharedPreferences.edit()
    .putString(RESULT_CHECKOUT_SESSION_ID, amazonCheckoutSessionId)
    .apply()


  fun getChargePermissionId(): String {
    val result = sharedPreferences.getString(RESULT_CHARGE_PERMISSION_ID, "") ?: ""
    return result.also {
      saveResult("")
    }
  }

  fun saveChargePermissionId(amazonChargePermissionId: String?) = sharedPreferences.edit()
    .putString(RESULT_CHARGE_PERMISSION_ID, amazonChargePermissionId)
    .apply()

  companion object {
    internal const val RESULT_CHECKOUT_SESSION_ID = "result_checkout_session_id"
    internal const val RESULT_CHARGE_PERMISSION_ID = "result_charge_permission_id"
  }
}
