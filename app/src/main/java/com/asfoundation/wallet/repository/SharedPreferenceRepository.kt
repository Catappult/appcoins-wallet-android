package com.asfoundation.wallet.repository

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SharedPreferenceRepository(context: Context) : PreferenceRepositoryType {

  private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

  override fun hasCompletedOnboarding(): Boolean {
    return pref.getBoolean(ONBOARDING_COMPLETE_KEY, false)
  }

  override fun setOnboardingComplete() {
    pref.edit()
        .putBoolean(ONBOARDING_COMPLETE_KEY, true)
        .apply()
  }

  override fun hasClickedSkipOnboarding(): Boolean {
    return pref.getBoolean(ONBOARDING_SKIP_CLICKED_KEY, false)
  }

  override fun setOnboardingSkipClicked() {
    pref.edit()
        .putBoolean(ONBOARDING_SKIP_CLICKED_KEY, true)
        .apply()
  }

  override fun getCurrentWalletAddress(): String? {
    return pref.getString(CURRENT_ACCOUNT_ADDRESS_KEY, null)
  }

  override fun setCurrentWalletAddress(address: String) {
    pref.edit()
        .putString(CURRENT_ACCOUNT_ADDRESS_KEY, address)
        .apply()
  }

  companion object {

    private const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"
    private const val ONBOARDING_COMPLETE_KEY = "onboarding_complete"
    private const val ONBOARDING_SKIP_CLICKED_KEY = "onboarding_skip_clicked"
  }
}
