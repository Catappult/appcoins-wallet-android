package com.asfoundation.wallet.ui.balance.database

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject


class BalanceDetailsMapper {

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  fun map(walletAddress: String): BalanceDetailsEntity = BalanceDetailsEntity(walletAddress)

  fun mapEthBalance(balance: BalanceDetailsEntity): Pair<Balance, FiatValue> {
    return Pair(Balance(WalletCurrency.ETHEREUM.symbol, getBigDecimal(balance.ethAmount)),
        FiatValue(getBigDecimal(balance.ethConversion), balance.fiatCurrency, balance.fiatSymbol))
  }

  fun mapAppcBalance(balance: BalanceDetailsEntity): Pair<Balance, FiatValue> {
    return Pair(Balance(WalletCurrency.APPCOINS.symbol, getBigDecimal(balance.appcAmount,
        CurrencyFormatUtils.APPC_SCALE)), FiatValue(getBigDecimal(balance.appcConversion),
        balance.fiatCurrency, balance.fiatSymbol))
  }

  fun mapCreditsBalance(balance: BalanceDetailsEntity): Pair<Balance, FiatValue> {
    return Pair(Balance(WalletCurrency.CREDITS.symbol,
        getBigDecimal(balance.creditsAmount, CurrencyFormatUtils.CREDITS_SCALE)),
        FiatValue(getBigDecimal(balance.creditsConversion), balance.fiatCurrency,
            balance.fiatSymbol))
  }

  private fun getBigDecimal(value: String, scale: Int = CurrencyFormatUtils.ETH_SCALE): BigDecimal {
    return if (value.isNotEmpty()) BigDecimal(value).setScale(scale, RoundingMode.FLOOR)
    else BigDecimal.ZERO
  }
}