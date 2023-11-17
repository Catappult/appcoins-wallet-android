package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class VkDataPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  companion object {
    const val VK_LOGIN_AUTH = "VK_LOGIN_AUTH"
  }

  fun getAuthVk() = sharedPreferences.getString(VK_LOGIN_AUTH, null)

  fun saveAuthVk(accessToken: String) {
    val editPreferences = sharedPreferences.edit()
    editPreferences.putString(VK_LOGIN_AUTH, accessToken)
    editPreferences.apply()
  }
}
