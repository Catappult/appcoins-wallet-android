package repository

import android.content.SharedPreferences
import io.reactivex.Completable
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = PreferencesRepositoryType::class)
class SharedPreferencesRepository @Inject constructor(private val pref: SharedPreferences) :
  PreferencesRepositoryType {

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

  override fun hasCompletedOnboarding() = pref.getBoolean(ONBOARDING_COMPLETE_KEY, false)

  override fun setOnboardingComplete() {
    pref.edit()
      .putBoolean(ONBOARDING_COMPLETE_KEY, true)
      .apply()
  }

  override fun hasClickedSkipOnboarding() = pref.getBoolean(ONBOARDING_SKIP_CLICKED_KEY, false)

  override fun setOnboardingSkipClicked() {
    pref.edit()
      .putBoolean(ONBOARDING_SKIP_CLICKED_KEY, true)
      .apply()
  }

  override fun getCurrentWalletAddress(): String? {
    return pref.getString(CURRENT_ACCOUNT_ADDRESS_KEY, null)
  }

  override fun addChangeListener(
    onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener
  ) {
    pref.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
  }

  override fun removeChangeListener(
    onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener
  ) {
    pref.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
  }

  override fun setCurrentWalletAddress(address: String) {
    pref.edit()
      .putString(CURRENT_ACCOUNT_ADDRESS_KEY, address)
      .apply()
  }

  override fun hasSeenPromotionTooltip(): Boolean {
    return pref.getBoolean(HAS_SEEN_PROMOTION_TOOLTIP, false)
  }

  override fun setHasSeenPromotionTooltip() {
    pref.edit()
      .putBoolean(HAS_SEEN_PROMOTION_TOOLTIP, true)
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

  override fun setUpdateNotificationSeenTime(currentTimeMillis: Long) {
    pref.edit()
      .putLong(UPDATE_SEEN_TIME, currentTimeMillis)
      .apply()
  }

  override fun getUpdateNotificationSeenTime() = pref.getLong(UPDATE_SEEN_TIME, -1)

  override fun getAndroidId() = pref.getString(ANDROID_ID, "")
    .orEmpty()


  override fun setAndroidId(androidId: String) {
    pref.edit()
      .putString(ANDROID_ID, androidId)
      .apply()
  }

  override fun getWalletPurchasesCount(walletAddress: String) =
    pref.getInt(WALLET_PURCHASES_COUNT + walletAddress, 0)

  override fun incrementWalletPurchasesCount(walletAddress: String, count: Int) =
    Completable.fromAction {
      pref.edit()
        .putInt(WALLET_PURCHASES_COUNT + walletAddress, count)
        .apply()
    }

  override fun setWalletId(walletId: String) {
    pref.edit()
      .putString(WALLET_ID, walletId)
      .apply()
  }

  override fun getWalletId() = pref.getString(WALLET_ID, null)

  override fun hasBeenInSettings(): Boolean = pref.getBoolean(HAS_BEEN_IN_SETTINGS, false)

  override fun setBeenInSettings() {
    pref.edit()
      .putBoolean(HAS_BEEN_IN_SETTINGS, true)
      .apply()
  }

  override fun increaseTimesOnHome() {
    pref.edit()
      .putInt(NUMBER_OF_TIMES_IN_HOME, pref.getInt(NUMBER_OF_TIMES_IN_HOME, 0) + 1)
      .apply()
  }

  override fun getNumberOfTimesOnHome(): Int = pref.getInt(NUMBER_OF_TIMES_IN_HOME, 0)
}
