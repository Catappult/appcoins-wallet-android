package com.asfoundation.wallet.ui.balance.database

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.ui.iab.FiatValue
import java.math.BigDecimal
import java.math.RoundingMode


class BalanceDetailsMapper {

  companion object {
    const val ETH_SYMBOL = "ETH"
    const val APPC_SYMBOL = "APPC"
    const val APPC_C_SYMBOL = "APPC-C"
    const val APPC_SCALE = 2
    const val ETHER_SCALE = 4
  }

  fun map(walletAddress: String): BalanceDetailsEntity {
    return BalanceDetailsEntity(walletAddress)
  }

  fun mapEthBalance(balance: BalanceDetailsEntity): Pair<Balance, FiatValue> {
    return Pair(Balance(ETH_SYMBOL, getBigDecimal(balance.ethAmount)),
        FiatValue(getBigDecimal(balance.ethConversion), balance.fiatCurrency, balance.fiatSymbol))
  }


  fun mapAppcBalance(balance: BalanceDetailsEntity): Pair<Balance, FiatValue> {
    return Pair(Balance(APPC_SYMBOL, getBigDecimal(balance.appcAmount, APPC_SCALE)),
        FiatValue(getBigDecimal(balance.appcConversion), balance.fiatCurrency,
            balance.fiatSymbol))
  }


  fun mapCreditsBalance(balance: BalanceDetailsEntity): Pair<Balance, FiatValue> {
    return Pair(Balance(APPC_C_SYMBOL, getBigDecimal(balance.creditsAmount, APPC_SCALE)),
        FiatValue(getBigDecimal(balance.creditsConversion), balance.fiatCurrency,
            balance.fiatSymbol))
  }

  private fun getBigDecimal(value: String, scale: Int = ETHER_SCALE): BigDecimal {
    return if (value.isNotEmpty()) BigDecimal(value).setScale(scale,
        RoundingMode.FLOOR) else BigDecimal("-1")
  }
}