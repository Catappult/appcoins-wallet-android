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

  override fun isFirstTimeOnTransactionActivity(): Boolean {
    return pref.getBoolean(FIRST_TIME_ON_TRANSACTION_ACTIVITY_KEY, false)
  }

  override fun setFirstTimeOnTransactionActivity() {
    pref.edit()
        .putBoolean(FIRST_TIME_ON_TRANSACTION_ACTIVITY_KEY, true)
        .apply()
  }

  override fun clearPoaNotificationSeenTime() {
    pref.edit()
        .remove(POA_LIMIT_SEEN_TIME)
        .apply()
  }

  override fun getPoaNotificationSeenTime(): Long {
    return pref.getLong(POA_LIMIT_SEEN_TIME, -1)
  }

  override fun setPoaNotificationSeenTime(currentTimeInMillis: Long) {
    pref.edit()
        .putLong(POA_LIMIT_SEEN_TIME, currentTimeInMillis)
        .apply()
  }

  override fun setWalletValidationStatus(walletAddress: String, validated: Boolean) {
    pref.edit()
        .putBoolean(WALLET_VERIFIED + walletAddress, validated)
        .apply()
  }

  override fun isWalletValidated(walletAddress: String): Boolean {
    return pref.getBoolean(WALLET_VERIFIED + walletAddress, false)
  }

  override fun removeWalletValidationStatus(walletAddress: String) {
    pref.edit()
        .remove(WALLET_VERIFIED + walletAddress)
        .apply()
  }

  override fun addWalletPreference(address: String?) {
    pref.edit()
        .putString(PREF_WALLET, address)
        .apply()
  }

  companion object {

    private const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"
    private const val ONBOARDING_COMPLETE_KEY = "onboarding_complete"
    private const val ONBOARDING_SKIP_CLICKED_KEY = "onboarding_skip_clicked"
    private const val FIRST_TIME_ON_TRANSACTION_ACTIVITY_KEY = "first_time_on_transaction_activity"
    private const val POA_LIMIT_SEEN_TIME = "poa_limit_seen_time"
    private const val WALLET_VERIFIED = "wallet_verified_"
    private const val PREF_WALLET = "pref_wallet"
  }
}
