package repository

import android.content.SharedPreferences
import javax.inject.Inject

class FiatCurrenciesSharedPreferences @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {

  companion object {
    private const val FIAT_CURRENCY = "fiat_currency"
    private const val SELECTED_FIRST_TIME = "selected_first_time"
    private const val CURRENCY_LIST_LAST_VERSION = "currency_list_last_version"
  }

  fun setCurrencyListLastVersion(versionCode: Int) {
    sharedPreferences.edit()
      .putInt(CURRENCY_LIST_LAST_VERSION, versionCode)
      .apply()
  }

  fun getCurrencyListLastVersion() = sharedPreferences.getInt(CURRENCY_LIST_LAST_VERSION, 0)

  fun getCachedSelectedCurrency(): String {
    return sharedPreferences.getString(FIAT_CURRENCY, "")!!
  }

  fun setSelectedCurrency(currency: String) {
    sharedPreferences.edit()
      .putString(FIAT_CURRENCY, currency)
      .apply()
  }

  fun getSelectCurrency() = sharedPreferences.getBoolean(SELECTED_FIRST_TIME, true)

  fun setSelectFirstTime() {
    sharedPreferences.edit()
      .putBoolean(SELECTED_FIRST_TIME, false)
      .apply()
  }
}