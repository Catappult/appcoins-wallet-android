package com.asfoundation.wallet.repository

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import io.reactivex.Completable
import io.reactivex.Single

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

  override fun saveAutoUpdateCardDismiss(updateVersionCode: Int): Completable {
    return Completable.fromCallable {
      pref.edit()
          .putInt(AUTO_UPDATE_VERSION, updateVersionCode)
          .apply()
    }
  }

  override fun getAutoUpdateCardDismissedVersion(): Single<Int> {
    return Single.fromCallable { pref.getInt(AUTO_UPDATE_VERSION, 0) }
  }

  companion object {

    private const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"
    private const val ONBOARDING_COMPLETE_KEY = "onboarding_complete"
    private const val ONBOARDING_SKIP_CLICKED_KEY = "onboarding_skip_clicked"
    private const val FIRST_TIME_ON_TRANSACTION_ACTIVITY_KEY = "first_time_on_transaction_activity"
    private const val AUTO_UPDATE_VERSION = "auto_update_version"
  }
}
