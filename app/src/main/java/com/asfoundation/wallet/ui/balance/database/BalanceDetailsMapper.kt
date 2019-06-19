package com.asfoundation.wallet.ui.balance.database

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.ui.iab.FiatValue
import java.math.BigDecimal


class BalanceDetailsMapper {
  fun map(walletAddress: String): BalanceDetailsEntity {
    return BalanceDetailsEntity(walletAddress)
  }

  fun mapEthBalance(balance: BalanceDetailsEntity): Pair<Balance, FiatValue> {
    return Pair(Balance("ETH", balance.ethAmount),
        FiatValue(getBigDecimal(balance.ethConversion), balance.fiatCurrency, balance.fiatSymbol))
  }


  fun mapAppcBalance(balance: BalanceDetailsEntity): Pair<Balance, FiatValue> {
    return Pair(Balance("APPC", balance.appcAmount),
        FiatValue(getBigDecimal(balance.appcConversion), balance.fiatCurrency, balance.fiatSymbol))
  }


  fun mapCreditsBalance(balance: BalanceDetailsEntity): Pair<Balance, FiatValue> {
    return Pair(Balance("APPC-C", balance.creditsAmount),
        FiatValue(getBigDecimal(balance.creditsConversion), balance.fiatCurrency, balance.fiatSymbol))
  }

  private fun getBigDecimal(value: String): BigDecimal {
    return if (value.isNotEmpty()) BigDecimal(value) else BigDecimal("-1")
  }
}