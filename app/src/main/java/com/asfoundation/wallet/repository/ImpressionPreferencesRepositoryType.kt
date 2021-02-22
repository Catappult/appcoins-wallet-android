package com.asfoundation.wallet.repository

interface ImpressionPreferencesRepositoryType {

  fun hasCompletedOnboarding(): Boolean

  fun setOnboardingComplete()

  fun hasClickedSkipOnboarding(): Boolean

  fun setOnboardingSkipClicked()

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