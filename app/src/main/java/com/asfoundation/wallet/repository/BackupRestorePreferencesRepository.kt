package com.asfoundation.wallet.repository

import android.content.SharedPreferences
import io.reactivex.Completable

class BackupRestorePreferencesRepository(private val pref: SharedPreferences) {

  companion object {
    private const val BACKUP_SEEN_TIME = "backup_seen_time_"
    private const val WALLET_IMPORT_BACKUP = "wallet_import_backup_"
    private const val HAS_SHOWN_BACKUP = "has_shown_backup_"
    private const val KEYSTORE_DIRECTORY = "keystore_directory"
    const val BACKED_UP_ONCE = "backed_up_once"
    private const val SEEN_BACKUP_SYSTEM_NOTIFICATION = "seen_backup_system_notification_"
  }

  fun getBackupNotificationSeenTime(walletAddress: String) =
      pref.getLong(BACKUP_SEEN_TIME + walletAddress, -1)

  fun setBackupNotificationSeenTime(walletAddress: String, currentTimeMillis: Long) {
    pref.edit()
        .putLong(BACKUP_SEEN_TIME + walletAddress, currentTimeMillis)
        .apply()
  }

  fun removeBackupNotificationSeenTime(walletAddress: String): Completable {
    return Completable.fromAction {
      pref.edit()
          .remove(BACKUP_SEEN_TIME + walletAddress)
          .apply()
    }
  }

  fun isWalletRestoreBackup(walletAddress: String) =
      pref.getBoolean(WALLET_IMPORT_BACKUP + walletAddress, false)

  fun setWalletRestoreBackup(walletAddress: String) {
    pref.edit()
        .putBoolean(WALLET_IMPORT_BACKUP + walletAddress, true)
        .apply()
  }

  fun removeWalletRestoreBackup(walletAddress: String): Completable {
    return Completable.fromAction {
      pref.edit()
          .remove(WALLET_IMPORT_BACKUP + walletAddress)
          .apply()
    }
  }

  fun hasShownBackup(walletAddress: String): Boolean {
    return pref.getBoolean(HAS_SHOWN_BACKUP + walletAddress, false)
  }

  fun setHasShownBackup(walletAddress: String, hasShown: Boolean) {
    pref.edit()
        .putBoolean(HAS_SHOWN_BACKUP + walletAddress, hasShown)
        .apply()
  }

  fun saveChosenUri(uri: String) {
    pref.edit()
        .putString(KEYSTORE_DIRECTORY, uri)
        .apply()
  }

  fun getChosenUri() = pref.getString(KEYSTORE_DIRECTORY, null)

  fun getBackedUpOnce() = pref.getBoolean(BACKED_UP_ONCE, false)

  fun saveBackedUpOnce() {
    pref.edit()
        .putBoolean(BACKED_UP_ONCE, true)
        .apply()
  }

  fun hasDismissedBackupSystemNotification(walletAddress: String) =
      pref.getBoolean(SEEN_BACKUP_SYSTEM_NOTIFICATION + walletAddress, false)

  fun setDismissedBackupSystemNotification(walletAddress: String) =
      pref.edit()
          .putBoolean(SEEN_BACKUP_SYSTEM_NOTIFICATION + walletAddress, true)
          .apply()

  fun removeChangeListener(
      onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener) {
    pref.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
  }

  fun addChangeListener(
      onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener) {
    pref.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
  }
}
