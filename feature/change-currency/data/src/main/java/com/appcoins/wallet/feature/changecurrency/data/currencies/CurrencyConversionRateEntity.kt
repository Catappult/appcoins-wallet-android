package com.appcoins.wallet.feature.changecurrency.data.currencies

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appcoins.wallet.feature.changecurrency.data.currencies.CurrencyConversionRateEntity.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
class CurrencyConversionRateEntity(@field:PrimaryKey @ColumnInfo(name = "currency_from")
                                   val currencyFrom: String,
                                   @ColumnInfo(name = "fiat_currency_to")
                                   val fiatCurrency: String = "",
                                   @ColumnInfo(name = "fiat_symbol") val fiatSymbol: String = "",
                                   @ColumnInfo(name = "conversion_rate")
                                   val rate: String = ZERO_RATE
) {

  override fun toString(): String {
    return ("CurrencyConversionRateEntity{"
        + "currencyFrom='"
        + currencyFrom
        + '\''.toString()
        + "fiatCurrency='"
        + fiatCurrency
        + '\''.toString()
        + ", fiatSymbol='"
        + fiatSymbol
        + '\''.toString()
        + ", rate='"
        + rate
        + '\''.toString()
        + '}'.toString())
  }

  companion object {

    internal const val TABLE_NAME = "currency_conversion_rates"
    internal const val APPC = "APPC"
    internal const val ETH = "ETH"
    internal const val ZERO_RATE = "0"
  }
}