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

  fun removeWalletValidationStatus(walletAddress: String): Completable

  fun saveAutoUpdateCardDismiss(updateVersionCode: Int): Completable

  fun getAutoUpdateCardDismissedVersion(): Single<Int>

  fun getUpdateNotificationSeenTime(): Long

  fun setUpdateNotificationSeenTime(currentTimeMillis: Long)

  fun getBackupNotificationSeenTime(walletAddress: String): Long

  fun setBackupNotificationSeenTime(walletAddress: String, currentTimeMillis: Long)

  fun removeBackupNotificationSeenTime(walletAddress: String): Completable

  fun isWalletRestoreBackup(walletAddress: String): Boolean

  fun setWalletRestoreBackup(walletAddress: String)

  fun removeWalletRestoreBackup(walletAddress: String): Completable

  fun hasShownBackup(walletAddress: String): Boolean

  fun setHasShownBackup(walletAddress: String, hasShown: Boolean)

  fun getAndroidId(): String

  fun setAndroidId(androidId: String)

  fun getGamificationLevel(): Int

  fun saveChosenUri(uri: String)

  fun getChosenUri(): String?

  fun getSeenBackupTooltip(): Boolean

  fun saveSeenBackupTooltip()

  fun hasDismissedBackupSystemNotification(walletAddress: String): Boolean

  fun setDismissedBackupSystemNotification(walletAddress: String)

  fun getWalletPurchasesCount(walletAddress: String): Int

  fun incrementWalletPurchasesCount(walletAddress: String, count: Int): Completable

  fun setWalletId(walletId: String)

  fun getWalletId(): String?
}
