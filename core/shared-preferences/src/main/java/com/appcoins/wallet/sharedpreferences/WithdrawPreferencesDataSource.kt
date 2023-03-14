package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class WithdrawPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  companion object {
    const val WITHDRAW_EMAIL_KEY = "WITHDRAW_EMAIL_KEY"
  }

  fun getUserEmail() = sharedPreferences.getString(WITHDRAW_EMAIL_KEY, null)

  fun saveUserEmail(email: String) {
    val editPreferences = sharedPreferences.edit()
    editPreferences.putString(WITHDRAW_EMAIL_KEY, email)
    editPreferences.apply()
  }
}
