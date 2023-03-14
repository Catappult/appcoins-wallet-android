package repository

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import javax.inject.Inject

class CommonsPreferencesDataSource @Inject constructor(private val sharedPreferences: SharedPreferences) {

  companion object {
    const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"
    private const val ONBOARDING_COMPLETE_KEY = "onboarding_complete"
    private const val ONBOARDING_SKIP_CLICKED_KEY = "onboarding_skip_clicked"

    //String was kept the same for legacy purposes
    private const val HAS_SEEN_PROMOTION_TOOLTIP = "first_time_on_transaction_activity"
    private const val AUTO_UPDATE_VERSION = "auto_update_version"
    private const val UPDATE_SEEN_TIME = "update_seen_time"
    private const val ANDROID_ID = "android_id"
    private const val WALLET_PURCHASES_COUNT = "wallet_purchases_count_"
    private const val WALLET_ID = "wallet_id"
    private const val HAS_BEEN_IN_SETTINGS = "has_been_in_settings"
    private const val NUMBER_OF_TIMES_IN_HOME = "number_of_times_in_home"
  }

  fun hasCompletedOnboarding() = sharedPreferences.getBoolean(ONBOARDING_COMPLETE_KEY, false)

  fun setOnboardingComplete() =
    sharedPreferences.edit().putBoolean(ONBOARDING_COMPLETE_KEY, true).apply()

  fun hasClickedSkipOnboarding() = sharedPreferences.getBoolean(ONBOARDING_SKIP_CLICKED_KEY, false)

  fun setOnboardingSkipClicked() =
    sharedPreferences.edit().putBoolean(ONBOARDING_SKIP_CLICKED_KEY, true).apply()

  fun getCurrentWalletAddress() = sharedPreferences.getString(CURRENT_ACCOUNT_ADDRESS_KEY, null)

  fun addChangeListener(onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener) =
    sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)

  fun removeChangeListener(onSharedPreferenceChangeListener: OnSharedPreferenceChangeListener) =
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)

  fun setCurrentWalletAddress(address: String) =
    sharedPreferences.edit().putString(CURRENT_ACCOUNT_ADDRESS_KEY, address).apply()

  fun hasSeenPromotionTooltip() = sharedPreferences.getBoolean(HAS_SEEN_PROMOTION_TOOLTIP, false)

  fun setHasSeenPromotionTooltip() =
    sharedPreferences.edit().putBoolean(HAS_SEEN_PROMOTION_TOOLTIP, true).apply()

  fun saveAutoUpdateCardDismiss(updateVersionCode: Int) =
    sharedPreferences.edit().putInt(AUTO_UPDATE_VERSION, updateVersionCode).apply()

  fun getAutoUpdateCardDismissedVersion() = sharedPreferences.getInt(AUTO_UPDATE_VERSION, 0)

  fun setUpdateNotificationSeenTime(currentTimeMillis: Long) =
    sharedPreferences.edit().putLong(UPDATE_SEEN_TIME, currentTimeMillis).apply()

  fun getUpdateNotificationSeenTime() = sharedPreferences.getLong(UPDATE_SEEN_TIME, -1)

  fun getAndroidId() = sharedPreferences.getString(ANDROID_ID, "").orEmpty()

  fun setAndroidId(androidId: String) =
    sharedPreferences.edit().putString(ANDROID_ID, androidId).apply()

  fun getWalletPurchasesCount(walletAddress: String) =
    sharedPreferences.getInt(WALLET_PURCHASES_COUNT + walletAddress, 0)

  fun incrementWalletPurchasesCount(walletAddress: String, count: Int) =
    sharedPreferences.edit().putInt(WALLET_PURCHASES_COUNT + walletAddress, count).apply()

  fun setWalletId(walletId: String) =
    sharedPreferences.edit().putString(WALLET_ID, walletId).apply()

  fun getWalletId() = sharedPreferences.getString(WALLET_ID, null)

  fun hasBeenInSettings() = sharedPreferences.getBoolean(HAS_BEEN_IN_SETTINGS, false)

  fun setBeenInSettings() =
    sharedPreferences.edit().putBoolean(HAS_BEEN_IN_SETTINGS, true).apply()

  fun increaseTimesOnHome() =
    sharedPreferences.edit()
      .putInt(NUMBER_OF_TIMES_IN_HOME, sharedPreferences.getInt(NUMBER_OF_TIMES_IN_HOME, 0) + 1)
      .apply()

  fun getNumberOfTimesOnHome() = sharedPreferences.getInt(NUMBER_OF_TIMES_IN_HOME, 0)
}
