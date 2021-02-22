package com.asfoundation.wallet.repository

import android.content.SharedPreferences

/**
 * Repository that includes preferences related to user impressions. Examples of this are:
 * - Screens visited;
 * - Notifications seen;
 * - Certain buttons or places clicked.
 */
class ImpressionPreferencesRepository(private val pref: SharedPreferences) :
    ImpressionPreferencesRepositoryType {

  companion object {

    private const val ONBOARDING_COMPLETE_KEY = "onboarding_complete"
    private const val ONBOARDING_SKIP_CLICKED_KEY = "onboarding_skip_clicked"

    //String was kept the same for legacy purposes
    private const val HAS_SEEN_PROMOTION_TOOLTIP = "first_time_on_transaction_activity"
    private const val HAS_BEEN_IN_PROMOTIONS_SCREEN = "has_been_in_promotions_screen"
    private const val HAS_BEEN_IN_SETTINGS = "has_been_in_settings"
    private const val NUMBER_OF_TIMES_IN_HOME = "number_of_times_in_home"
    private const val HAS_SEEN_VOUCHER_TOOLTIP = "has_seen_voucher_tooltip"
  }

  override fun hasCompletedOnboarding() = pref.getBoolean(ONBOARDING_COMPLETE_KEY, false)

  override fun setOnboardingComplete() {
    pref.edit()
        .putBoolean(ONBOARDING_COMPLETE_KEY, true)
        .apply()
  }

  override fun hasClickedSkipOnboarding() =
      pref.getBoolean(ONBOARDING_SKIP_CLICKED_KEY, false)

  override fun setOnboardingSkipClicked() {
    pref.edit()
        .putBoolean(ONBOARDING_SKIP_CLICKED_KEY, true)
        .apply()
  }

  override fun hasSeenPromotionTooltip() =
      pref.getBoolean(HAS_SEEN_PROMOTION_TOOLTIP, false)

  override fun setHasSeenPromotionTooltip() {
    pref.edit()
        .putBoolean(HAS_SEEN_PROMOTION_TOOLTIP, true)
        .apply()
  }

  override fun hasBeenInSettings() = pref.getBoolean(HAS_BEEN_IN_SETTINGS, false)

  override fun setHasBeenInSettings() {
    pref.edit()
        .putBoolean(HAS_BEEN_IN_SETTINGS, true)
        .apply()
  }

  override fun getNumberOfTimesOnHome() = pref.getInt(NUMBER_OF_TIMES_IN_HOME, 0)

  override fun increaseTimesOnHome() {
    pref.edit()
        .putInt(NUMBER_OF_TIMES_IN_HOME, pref.getInt(
            NUMBER_OF_TIMES_IN_HOME, 0) + 1)
        .apply()
  }

  override fun hasBeenInPromotionsScreen() =
      pref.getBoolean(HAS_BEEN_IN_PROMOTIONS_SCREEN, false)

  override fun setHasBeenInPromotionsScreen() {
    pref.edit()
        .putBoolean(HAS_BEEN_IN_PROMOTIONS_SCREEN, true)
        .apply()
  }

  override fun hasSeenVoucherTooltip() = pref.getBoolean(HAS_SEEN_VOUCHER_TOOLTIP, false)

  override fun setHasSeenVoucherTooltip() {
    pref.edit()
        .putBoolean(HAS_SEEN_VOUCHER_TOOLTIP, true)
        .apply()
  }
}