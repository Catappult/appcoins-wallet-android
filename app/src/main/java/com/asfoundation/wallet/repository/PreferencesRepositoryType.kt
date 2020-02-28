package com.asfoundation.wallet.repository

import io.reactivex.Completable
import io.reactivex.Single

interface PreferencesRepositoryType {

  fun hasCompletedOnboarding(): Boolean
  fun setOnboardingComplete()
  fun hasClickedSkipOnboarding(): Boolean
  fun setOnboardingSkipClicked()
  fun getCurrentWalletAddress(): String?
  fun setCurrentWalletAddress(address: String)
  fun isFirstTimeOnTransactionActivity(): Boolean
  fun setFirstTimeOnTransactionActivity()
  fun getPoaNotificationSeenTime(): Long
  fun clearPoaNotificationSeenTime()
  fun setPoaNotificationSeenTime(currentTimeInMillis: Long)
  fun setWalletValidationStatus(walletAddress: String, validated: Boolean)
  fun isWalletValidated(walletAddress: String): Boolean
  fun removeWalletValidationStatus(walletAddress: String)
  fun addWalletPreference(address: String?)
  fun saveAutoUpdateCardDismiss(updateVersionCode: Int): Completable
  fun getAutoUpdateCardDismissedVersion(): Single<Int>
  fun getUpdateNotificationSeenTime(): Long
  fun setUpdateNotificationSeenTime(currentTimeMillis: Long)
  fun getBackupNotificationSeenTime(walletAddress: String): Long
  fun setBackupNotificationSeenTime(walletAddress: String, currentTimeMillis: Long)
  fun removeBackupNotificationSeenTime(walletAddress: String)
  fun isWalletImportBackup(walletAddress: String): Boolean
  fun setWalletImportBackup(walletAddress: String)
  fun removeWalletImportBackup(walletAddress: String)
  fun hasShownBackup(walletAddress: String): Boolean
  fun setHasShownBackup(walletAddress: String, hasShown: Boolean)
  fun getAndroidId(): String
  fun setAndroidId(androidId: String)
}
