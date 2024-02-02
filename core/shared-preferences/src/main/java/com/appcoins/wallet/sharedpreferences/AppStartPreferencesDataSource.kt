package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class AppStartPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  fun getRunCount(): Int = sharedPreferences.getInt(RUNS_COUNT, 0)

  fun saveRunCount(count: Int) = sharedPreferences.edit()
    .putInt(RUNS_COUNT, count)
    .apply()

  fun getIsFirstPayment(): Boolean = sharedPreferences.getBoolean(IS_FIRST_PAYMENT, true)

  fun saveIsFirstPayment(isFirstPayment: Boolean) = sharedPreferences.edit()
    .putBoolean(IS_FIRST_PAYMENT, isFirstPayment)
    .apply()

  companion object {
    internal const val RUNS_COUNT = "AppStartRepository.RunsCount"
    internal const val IS_FIRST_PAYMENT = "is_first_payment"
  }
}