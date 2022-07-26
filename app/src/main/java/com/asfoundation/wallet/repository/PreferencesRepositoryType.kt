package com.asfoundation.wallet.repository

import android.content.SharedPreferences
import io.reactivex.Completable
import io.reactivex.Single

interface PreferencesRepositoryType {
  fun hasSentFirstLaunchEvent(): Boolean

  fun setFirstLaunchEvent()

  fun hasCompletedOnboarding(): Boolean

  fun setOnboardingComplete()

  fun isOnboardingFromIap(): Boolean

  fun setOnboardingFromIapState(state: Boolean)

  fun getOnboardingFromIapPackageName(): String?

  fun setOnboardingFromIapPackageName(appPackageName: String)

  fun hasClickedSkipOnboarding(): Boolean

  fun setOnboardingSkipClicked()

  fun getCurrentWalletAddress(): String?

  fun addChangeListener(
    onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener
  )

  fun removeChangeListener(
    onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener
  )

  fun setCurrentWalletAddress(address: String)

  fun hasSeenPromotionTooltip(): Boolean

  fun setHasSeenPromotionTooltip()

  fun saveAutoUpdateCardDismiss(updateVersionCode: Int): Completable

  fun getAutoUpdateCardDismissedVersion(): Single<Int>

  fun getUpdateNotificationSeenTime(): Long

  fun setUpdateNotificationSeenTime(currentTimeMillis: Long)

  fun getAndroidId(): String

  fun setAndroidId(androidId: String)

  fun getWalletPurchasesCount(walletAddress: String): Int

  fun incrementWalletPurchasesCount(walletAddress: String, count: Int): Completable

  fun setWalletId(walletId: String)

  fun getWalletId(): String?

  fun hasBeenInSettings(): Boolean

  fun setBeenInSettings()

  fun increaseTimesOnHome()

  fun getNumberOfTimesOnHome(): Int
}
