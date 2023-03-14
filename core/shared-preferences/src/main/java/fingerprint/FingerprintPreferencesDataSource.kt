package fingerprint

import android.content.SharedPreferences
import javax.inject.Inject


class FingerprintPreferencesDataSource @Inject constructor(private val sharedPreferences: SharedPreferences) {

  private companion object {
    private const val AUTHENTICATION_PERMISSION = "authentication_permission"
    private const val AUTHENTICATION_ERROR_TIME = "authentication_error_time"
    private const val HAS_SEEN_FINGERPRINT_TOOLTIP = "has_seen_fingerprint_tooltip"
  }

  fun setAuthenticationPermission(result: Boolean) =
    sharedPreferences.edit()
      .putBoolean(AUTHENTICATION_PERMISSION, result)
      .apply()

  fun hasAuthenticationPermission() = sharedPreferences.getBoolean(AUTHENTICATION_PERMISSION, false)

  fun setAuthenticationErrorTime(timer: Long) =
    sharedPreferences.edit()
      .putLong(AUTHENTICATION_ERROR_TIME, timer)
      .apply()

  fun getAuthenticationErrorTime() = sharedPreferences.getLong(AUTHENTICATION_ERROR_TIME, 0)

  fun hasSeenFingerprintTooltip() =
    sharedPreferences.getBoolean(HAS_SEEN_FINGERPRINT_TOOLTIP, false)

  fun setSeenFingerprintTooltip() =
    sharedPreferences.edit()
      .putBoolean(HAS_SEEN_FINGERPRINT_TOOLTIP, true)
      .apply()
}