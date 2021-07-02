package com.asfoundation.wallet.ui.settings.change_currency

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson

class SelectedCurrencyInteract(private val pref: SharedPreferences) {

  private companion object {
    private const val FIAT_CURRENCY = "fiat_currency"
  }

  fun setSelectedCurrency(fiatCurrency: FiatCurrency) {
    Log.d("APPC-2472", "SelectedCurrencyInteract: setSelectedCurrency: $fiatCurrency")
    pref.edit()
        .putString(FIAT_CURRENCY, Gson().toJson(fiatCurrency))
        .apply()
  }

  fun getSelectedCurrency(): FiatCurrency? {
    Log.d("APPC-2472", "SelectedCurrencyInteract: getSelectedCurrency: ${
      Gson().fromJson(pref.getString(FIAT_CURRENCY, ""), FiatCurrency::class.java)
    }")
    return if (selectedCurrencyPrefExists()) {
      Gson().fromJson(pref.getString(FIAT_CURRENCY, ""), FiatCurrency::class.java)
    } else {
      null
    }
  }

  private fun selectedCurrencyPrefExists(): Boolean {
    return pref.contains(FIAT_CURRENCY)
  }
}