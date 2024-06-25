package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class EmailPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  companion object {
    const val WALLET_EMAIL_SAVED = "WALLET_EMAIL_SAVED"
    const val IS_HIDE_WALLET_EMAIL_CARD = "IS_HIDE_WALLET_EMAIL_CARD"
  }

  fun getWalletEmail() = sharedPreferences.getString(WALLET_EMAIL_SAVED, "")

  fun saveWalletEmail(email: String) {
    val editPreferences = sharedPreferences.edit()
    editPreferences.putString(WALLET_EMAIL_SAVED, email)
    editPreferences.apply()
  }


  fun isHideWalletEmailCard() = sharedPreferences.getBoolean(IS_HIDE_WALLET_EMAIL_CARD, false)

  fun saveHideWalletEmailCard(hasEmailSaved: Boolean) {
    val editPreferences = sharedPreferences.edit()
    editPreferences.putBoolean(IS_HIDE_WALLET_EMAIL_CARD, hasEmailSaved)
    editPreferences.apply()
  }
}
