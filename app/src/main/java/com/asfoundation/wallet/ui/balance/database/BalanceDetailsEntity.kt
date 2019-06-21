package com.asfoundation.wallet.ui.balance.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.asfoundation.wallet.ui.balance.database.BalanceDetailsEntity.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
class BalanceDetailsEntity(@field:PrimaryKey @field:ColumnInfo(name = "wallet_address")
                           val wallet: String) {
  @ColumnInfo(name = "fiat_currency")
  var fiatCurrency: String = ""
  @ColumnInfo(name = "fiat_symbol")
  var fiatSymbol: String = ""
  @ColumnInfo(name = "eth_token_amount")
  var ethAmount: String = ""
  @ColumnInfo(name = "eth_token_conversion")
  var ethConversion: String = ""
  @ColumnInfo(name = "appc_token_amount")
  var appcAmount: String = ""
  @ColumnInfo(name = "appc_token_conversion")
  var appcConversion: String = ""
  @ColumnInfo(name = "credits_token_amount")
  var creditsAmount: String = ""
  @ColumnInfo(name = "credits_token_conversion")
  var creditsConversion: String = ""

  override fun toString(): String {
    return ("BalanceDetailsEntity{"
        + "wallet='"
        + wallet
        + '\''.toString()
        + ", fiatCurrency='"
        + fiatCurrency
        + '\''.toString()
        + ", fiatSymbol='"
        + fiatSymbol
        + '\''.toString()
        + ", ethAmount='"
        + ethAmount
        + '\''.toString()
        + ", ethConversion='"
        + ethConversion
        + '\''.toString()
        + ", appcAmount='"
        + appcAmount
        + '\''.toString()
        + ", appcConversion='"
        + appcConversion
        + '\''.toString()
        + ", creditsAmount='"
        + creditsAmount
        + '\''.toString()
        + ", creditsConversion='"
        + creditsConversion
        + '\''.toString()
        + '}'.toString())
  }

  companion object {

    internal const val TABLE_NAME = "balance"
  }
}
