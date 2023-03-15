package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class OemIdPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences,
) {
  fun putOemId(key: String?, value: String?) =
    sharedPreferences.edit()
      .putString(key, value)
      .apply()

  fun getOemId(key: String?) = sharedPreferences.getString(key, "")
}