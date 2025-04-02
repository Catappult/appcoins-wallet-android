package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject
import androidx.core.content.edit

class FiatCurrenciesPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  fun setCurrencyListLastVersion(versionCode: Int) =
    sharedPreferences.edit() {
      putInt(CURRENCY_LIST_LAST_VERSION, versionCode)
    }

  fun getCurrencyListLastVersion() = sharedPreferences.getInt(CURRENCY_LIST_LAST_VERSION, 0)

  fun getCachedSelectedCurrency() = sharedPreferences.getString(FIAT_CURRENCY, null)

  fun getCachedSelectedCurrencySymbol() = sharedPreferences.getString(FIAT_CURRENCY_SYMBOL, null)

  fun setSelectedCurrency(currency: String) =
    sharedPreferences.edit() { putString(FIAT_CURRENCY, currency) }

  fun setSelectedCurrencySymbol(symbol: String) =
    sharedPreferences.edit() { putString(FIAT_CURRENCY_SYMBOL, symbol) }

  fun getSelectCurrency() = sharedPreferences.getBoolean(SELECTED_FIRST_TIME, true)

  fun getSelectCurrencySymbol() = sharedPreferences.getBoolean(SELECTED_FIRST_TIME_SYMBOL, true)

  fun setSelectFirstTimeSymbol() = sharedPreferences.edit() {
    putBoolean(
      SELECTED_FIRST_TIME_SYMBOL,
      false
    )
  }

  fun setSelectFirstTime() = sharedPreferences.edit() { putBoolean(SELECTED_FIRST_TIME, false) }

  companion object {
    private const val FIAT_CURRENCY = "fiat_currency"
    private const val FIAT_CURRENCY_SYMBOL = "fiat_currency_symbol"
    private const val SELECTED_FIRST_TIME = "selected_first_time"
    private const val SELECTED_FIRST_TIME_SYMBOL = "selected_first_time_symbol"
    private const val CURRENCY_LIST_LAST_VERSION = "currency_list_last_version"
  }

}