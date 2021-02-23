package com.asfoundation.wallet.repository

import io.reactivex.Completable
import io.reactivex.Single

interface ImpressionPreferencesRepositoryType {

  fun hasCompletedOnboarding(): Boolean

  fun setOnboardingComplete()

  fun hasClickedSkipOnboarding(): Boolean

  fun setOnboardingSkipClicked()

  fun getPoaNotificationSeenTime(): Long

  fun setPoaNotificationSeenTime(currentTimeInMillis: Long)

  fun clearPoaNotificationSeenTime()

  fun getAutoUpdateCardDismissedVersion(): Single<Int>

  fun saveAutoUpdateCardDismiss(updateVersionCode: Int): Completable

  fun getUpdateNotificationSeenTime(): Long

  fun setUpdateNotificationSeenTime(currentTimeMillis: Long)

  fun hasSeenPromotionTooltip(): Boolean

  fun setHasSeenPromotionTooltip()

  fun hasBeenInSettings(): Boolean

  fun setHasBeenInSettings()

  fun getNumberOfTimesOnHome(): Int

  fun increaseTimesOnHome()

  fun hasBeenInPromotionsScreen(): Boolean

  fun setHasBeenInPromotionsScreen()

  fun hasSeenVoucherTooltip(): Boolean

  fun setHasSeenVoucherTooltip()
}