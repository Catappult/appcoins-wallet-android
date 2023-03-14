package repository

import android.content.SharedPreferences
import java.math.BigDecimal
import javax.inject.Inject

class GasPreferenceRepository @Inject constructor(private val pref: SharedPreferences) {

  companion object {
    private const val GAS_PRICE = "gas_price"
    private const val GAS_LIMIT = "gas_limit"
  }

  fun saveGasPrice(price: BigDecimal) {
    pref.edit()
        .putLong(GAS_PRICE, price.toLong())
        .apply()
  }

  fun saveGasLimit(limit: BigDecimal) {
    pref.edit()
        .putLong(GAS_LIMIT, limit.toLong())
        .apply()
  }

  fun getSavedGasPrice(): BigDecimal? {
    val price = pref.getLong(GAS_PRICE, -1)
    return if (price == -1L) null
    else BigDecimal(price)
  }

  fun getSavedGasLimit(): BigDecimal? {
    val limit = pref.getLong(GAS_LIMIT, -1)
    return if (limit == -1L) null
    else BigDecimal(limit)
  }
}
