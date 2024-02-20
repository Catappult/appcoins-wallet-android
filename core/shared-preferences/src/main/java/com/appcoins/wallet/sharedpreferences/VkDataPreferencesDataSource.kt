package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class VkDataPreferencesDataSource
@Inject
constructor(private val sharedPreferences: SharedPreferences) {
  companion object {
    const val VK_LOGIN_AUTH = "VK_LOGIN_AUTH"
    const val VK_LOGIN_EMAIL = "VK_LOGIN_EMAIL"
    const val VK_LOGIN_PHONE = "VK_LOGIN_PHONE"
  }

  fun getAuthVk() = sharedPreferences.getString(VK_LOGIN_AUTH, null)

  fun getEmailVK() = sharedPreferences.getString(VK_LOGIN_EMAIL, "") ?: ""

  fun getPhoneVK() = sharedPreferences.getString(VK_LOGIN_PHONE, "") ?: ""

  fun saveAuthVk(accessToken: String, email: String, phone: String) {
    val editPreferences = sharedPreferences.edit()
    editPreferences.putString(VK_LOGIN_AUTH, accessToken)
    editPreferences.putString(VK_LOGIN_EMAIL, email)
    editPreferences.putString(VK_LOGIN_PHONE, phone)
    editPreferences.apply()
  }
}
