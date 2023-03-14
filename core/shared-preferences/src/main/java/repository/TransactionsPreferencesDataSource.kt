package repository

import android.content.SharedPreferences
import javax.inject.Inject

class TransactionsPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  fun isOldTransactionsLoaded() = sharedPreferences.getBoolean(OLD_TRANSACTIONS_LOAD, false)

  fun oldTransactionsLoaded() =
    sharedPreferences.edit().putBoolean(OLD_TRANSACTIONS_LOAD, true).apply()

  fun setLocale(locale: String) = sharedPreferences.edit().putString(LAST_LOCALE, locale).apply()

  fun getLastLocale() = sharedPreferences.getString(LAST_LOCALE, null)

  companion object {
    private const val OLD_TRANSACTIONS_LOAD = "IS_OLD_TRANSACTIONS_LOADED"
    private const val LAST_LOCALE = "locale"
  }
}