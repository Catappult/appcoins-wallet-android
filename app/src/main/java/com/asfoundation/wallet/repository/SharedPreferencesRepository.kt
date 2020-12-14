package com.asfoundation.wallet.repository

import android.content.SharedPreferences
import io.reactivex.Completable
import io.reactivex.Single

class SharedPreferencesRepository(private val pref: SharedPreferences) : PreferencesRepositoryType {

  companion object {

    private const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"
    private const val ONBOARDING_COMPLETE_KEY = "onboarding_complete"
    private const val ONBOARDING_SKIP_CLICKED_KEY = "onboarding_skip_clicked"

    //String was kept the same for legacy purposes
    private const val HAS_SEEN_PROMOTION_TOOLTIP = "first_time_on_transaction_activity"
    private const val AUTO_UPDATE_VERSION = "auto_update_version"
    private const val POA_LIMIT_SEEN_TIME = "poa_limit_seen_time"
    private const val UPDATE_SEEN_TIME = "update_seen_time"
    private const val BACKUP_SEEN_TIME = "backup_seen_time_"
    private const val PROMOTION_SEEN_TIME = "promotion_seen_time_"
    private const val WALLET_VERIFIED = "wallet_verified_"
    private const val WALLET_IMPORT_BACKUP = "wallet_import_backup_"
    private const val HAS_SHOWN_BACKUP = "has_shown_backup_"
    private const val ANDROID_ID = "android_id"
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val KEYSTORE_DIRECTORY = "keystore_directory"
    private const val SEEN_BACKUP_TOOLTIP = "seen_backup_tooltip"
    private const val SEEN_BACKUP_SYSTEM_NOTIFICATION = "seen_backup_system_notification_"
    private const val WALLET_PURCHASES_COUNT = "wallet_purchases_count_"
    private const val WALLET_ID = "wallet_id"
    private const val SHOW_GAMIFICATION_DISCLAIMER = "SHOW_GAMIFICATION_DISCLAIMER"
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

  override fun clearPoaNotificationSeenTime() {
    pref.edit()
        .remove(POA_LIMIT_SEEN_TIME)
        .apply()
  }

  override fun getPoaNotificationSeenTime() = pref.getLong(POA_LIMIT_SEEN_TIME, -1)

  override fun setPoaNotificationSeenTime(currentTimeInMillis: Long) {
    pref.edit()
        .putLong(POA_LIMIT_SEEN_TIME, currentTimeInMillis)
        .apply()
  }

  override fun setUpdateNotificationSeenTime(currentTimeMillis: Long) {
    pref.edit()
        .putLong(UPDATE_SEEN_TIME, currentTimeMillis)
        .apply()
  }

  override fun getUpdateNotificationSeenTime() = pref.getLong(UPDATE_SEEN_TIME, -1)

  override fun setWalletValidationStatus(walletAddress: String, validated: Boolean) {
    pref.edit()
        .putBoolean(WALLET_VERIFIED + walletAddress, validated)
        .apply()
  }

  override fun isWalletValidated(walletAddress: String) =
      pref.getBoolean(WALLET_VERIFIED + walletAddress, false)

  override fun removeWalletValidationStatus(walletAddress: String): Completable {
    return Completable.fromAction {
      pref.edit()
          .remove(WALLET_VERIFIED + walletAddress)
          .apply()
    }
  }

  override fun getBackupNotificationSeenTime(walletAddress: String) =
      pref.getLong(BACKUP_SEEN_TIME + walletAddress, -1)

  override fun setBackupNotificationSeenTime(walletAddress: String, currentTimeMillis: Long) {
    pref.edit()
        .putLong(BACKUP_SEEN_TIME + walletAddress, currentTimeMillis)
        .apply()
  }

  override fun removeBackupNotificationSeenTime(walletAddress: String): Completable {
    return Completable.fromAction {
      pref.edit()
          .remove(BACKUP_SEEN_TIME + walletAddress)
          .apply()
    }
  }

  override fun setPromotionNotificationSeenTime(walletAddress: String, currentTimeMillis: Long) {
    pref.edit()
        .putLong(PROMOTION_SEEN_TIME + walletAddress, currentTimeMillis)
        .apply()
  }

  override fun removePromotionNotificationSeenTime(walletAddress: String): Completable {
    return Completable.fromAction {
      pref.edit()
          .remove(PROMOTION_SEEN_TIME + walletAddress)
          .apply()
    }
  }

  override fun isWalletRestoreBackup(walletAddress: String) =
      pref.getBoolean(WALLET_IMPORT_BACKUP + walletAddress, false)

  override fun setWalletRestoreBackup(walletAddress: String) {
    pref.edit()
        .putBoolean(WALLET_IMPORT_BACKUP + walletAddress, true)
        .apply()
  }

  override fun removeWalletRestoreBackup(walletAddress: String): Completable {
    return Completable.fromAction {
      pref.edit()
          .remove(WALLET_IMPORT_BACKUP + walletAddress)
          .apply()
    }
  }

  override fun hasShownBackup(walletAddress: String): Boolean {
    return pref.getBoolean(HAS_SHOWN_BACKUP + walletAddress, false)
  }

  override fun setHasShownBackup(walletAddress: String, hasShown: Boolean) {
    pref.edit()
        .putBoolean(HAS_SHOWN_BACKUP + walletAddress, hasShown)
        .apply()
  }

  override fun getAndroidId() = pref.getString(ANDROID_ID, "")
      .orEmpty()


  override fun setAndroidId(androidId: String) {
    pref.edit()
        .putString(ANDROID_ID, androidId)
        .apply()
  }

  override fun getGamificationLevel() = pref.getInt(GAMIFICATION_LEVEL, -1)

  override fun saveChosenUri(uri: String) {
    pref.edit()
        .putString(KEYSTORE_DIRECTORY, uri)
        .apply()
  }

  override fun getChosenUri() = pref.getString(KEYSTORE_DIRECTORY, null)

  override fun getSeenBackupTooltip() = pref.getBoolean(SEEN_BACKUP_TOOLTIP, false)

  override fun saveSeenBackupTooltip() {
    pref.edit()
        .putBoolean(SEEN_BACKUP_TOOLTIP, true)
        .apply()
  }

  override fun hasDismissedBackupSystemNotification(walletAddress: String) =
      pref.getBoolean(SEEN_BACKUP_SYSTEM_NOTIFICATION + walletAddress, false)

  override fun setDismissedBackupSystemNotification(walletAddress: String) =
      pref.edit()
          .putBoolean(SEEN_BACKUP_SYSTEM_NOTIFICATION + walletAddress, true)
          .apply()

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

  override fun shouldShowGamificationDisclaimer() = pref.getBoolean(SHOW_GAMIFICATION_DISCLAIMER, true)

  override fun setGamificationDisclaimerShown() {
    pref.edit()
        .putBoolean(SHOW_GAMIFICATION_DISCLAIMER, false)
        .apply()
  }

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
