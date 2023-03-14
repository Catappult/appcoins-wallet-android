package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import java.math.BigDecimal
import javax.inject.Inject

class GasPreferencesDataSource @Inject constructor(private val sharedPreferences: SharedPreferences) {
  fun saveGasPrice(price: BigDecimal) =
    sharedPreferences.edit()
      .putLong(GAS_PRICE, price.toLong())
      .apply()

  fun saveGasLimit(limit: BigDecimal) =
    sharedPreferences.edit()
      .putLong(GAS_LIMIT, limit.toLong())
      .apply()

  fun getSavedGasPrice(): BigDecimal? {
    val price = sharedPreferences.getLong(GAS_PRICE, -1)
    return if (price == -1L) null
    else BigDecimal(price)
  }

  fun getSavedGasLimit(): BigDecimal? {
    val limit = sharedPreferences.getLong(GAS_LIMIT, -1)
    return if (limit == -1L) null
    else BigDecimal(limit)
  }

  companion object {
    private const val GAS_PRICE = "gas_price"
    private const val GAS_LIMIT = "gas_limit"
  }
}
