package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class CardPaymentDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {

  fun isMandatoryCvc(): Boolean {
    return sharedPreferences.getBoolean(MANDATORY_CVC, false)
  }

  fun setMandatoryCvc(isMandatory: Boolean) {
    sharedPreferences.edit()
      .putBoolean(MANDATORY_CVC, isMandatory)
      .apply()
  }

  companion object {
    private const val MANDATORY_CVC = "mandatory_cvc"
  }
}
