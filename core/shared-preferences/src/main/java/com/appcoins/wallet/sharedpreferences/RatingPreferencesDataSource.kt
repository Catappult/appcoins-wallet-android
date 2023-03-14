package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class RatingPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  companion object {
    const val REMIND_ME_LATER_TIME_KEY = "first_time_rating"
    const val IMPRESSION_KEY = "impression_rating"
    const val HAS_ENOUGH_SUCCESSFUL_TRANSACTIONS_ = "has_enough_successful_transactions"
    private const val MONTH = 30L * 24 * 60 * 60 * 1000
  }

  fun saveEnoughSuccessfulTransactions() =
    sharedPreferences.edit()
      .putBoolean(HAS_ENOUGH_SUCCESSFUL_TRANSACTIONS_, true)
      .apply()

  fun hasEnoughSuccessfulTransactions() =
    sharedPreferences.getBoolean(HAS_ENOUGH_SUCCESSFUL_TRANSACTIONS_, false)

  fun getRemindMeLaterDate() = sharedPreferences.getLong(REMIND_ME_LATER_TIME_KEY, -1L)

  fun hasSeenDialog() = sharedPreferences.getBoolean(IMPRESSION_KEY, false)

  fun setImpression() =
    sharedPreferences.edit()
      .putBoolean(IMPRESSION_KEY, true)
      .putLong(REMIND_ME_LATER_TIME_KEY, -1L)
      .apply()

  fun setRemindMeLater() =
    sharedPreferences.edit()
      .putLong(REMIND_ME_LATER_TIME_KEY, System.currentTimeMillis() + MONTH)
      .apply()
}