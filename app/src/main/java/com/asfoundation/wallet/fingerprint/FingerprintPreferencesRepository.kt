package com.asfoundation.wallet.fingerprint

import android.content.SharedPreferences

class FingerprintPreferencesRepository(private val pref: SharedPreferences) :
    FingerprintPreferencesRepositoryContract {

  private companion object {
    private const val AUTHENTICATION_PERMISSION = "authentication_permission"
    private const val AUTHENTICATION_ERROR_TIME = "authentication_error_time"
    private const val HAS_SEEN_FINGERPRINT_TOOLTIP = "has_seen_fingerprint_tooltip"
  }

  override fun setAuthenticationPermission(result: Boolean) {
    pref.edit()
        .putBoolean(AUTHENTICATION_PERMISSION, result)
        .apply()
  }

  override fun hasAuthenticationPermission(): Boolean {
    return pref.getBoolean(AUTHENTICATION_PERMISSION, false)
  }

  override fun setAuthenticationErrorTime(timer: Long) {
    pref.edit()
        .putLong(AUTHENTICATION_ERROR_TIME, timer)
        .apply()
  }

  override fun getAuthenticationErrorTime() = pref.getLong(AUTHENTICATION_ERROR_TIME, 0)

  override fun hasSeenFingerprintTooltip(): Boolean {
    return pref.getBoolean(HAS_SEEN_FINGERPRINT_TOOLTIP, false)
  }

  override fun setSeenFingerprintTooltip() {
    pref.edit()
        .putBoolean(HAS_SEEN_FINGERPRINT_TOOLTIP, true)
        .apply()
  }
}