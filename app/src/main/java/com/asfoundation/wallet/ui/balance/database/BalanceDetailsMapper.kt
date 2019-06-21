package com.asfoundation.wallet.ui.balance.database

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.ui.iab.FiatValue
import java.math.BigDecimal


class BalanceDetailsMapper {

  companion object {
    const val ETH_SYMBOL = "ETH"
    const val APPC_SYMBOL = "APPC"
    const val APPC_C_SYMBOL = "APPC-C"
  }

  fun map(walletAddress: String): BalanceDetailsEntity {
    return BalanceDetailsEntity(walletAddress)
  }

  fun mapEthBalance(balance: BalanceDetailsEntity): Pair<Balance, FiatValue> {
    return Pair(Balance(ETH_SYMBOL, balance.ethAmount),
        FiatValue(getBigDecimal(balance.ethConversion), balance.fiatCurrency, balance.fiatSymbol))
  }


  fun mapAppcBalance(balance: BalanceDetailsEntity): Pair<Balance, FiatValue> {
    return Pair(Balance(APPC_SYMBOL, balance.appcAmount),
        FiatValue(getBigDecimal(balance.appcConversion), balance.fiatCurrency, balance.fiatSymbol))
  }


  fun mapCreditsBalance(balance: BalanceDetailsEntity): Pair<Balance, FiatValue> {
    return Pair(Balance(APPC_C_SYMBOL, balance.creditsAmount),
        FiatValue(getBigDecimal(balance.creditsConversion), balance.fiatCurrency, balance.fiatSymbol))
  }

  private fun getBigDecimal(value: String): BigDecimal {
    return if (value.isNotEmpty()) BigDecimal(value) else BigDecimal("-1")
  }
}