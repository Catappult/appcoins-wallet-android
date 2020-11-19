package com.asfoundation.wallet.fingerprint

interface FingerprintPreferenceRepositoryContract {

  fun setAuthenticationPermission(result: Boolean)

  fun hasAuthenticationPermission(): Boolean

  fun setAuthenticationErrorTime(timer: Long)

  fun getAuthenticationErrorTime(): Long

  fun hasSeenFingerprintTooltip(): Boolean

  fun setSeenFingerprintTooltip()
}
