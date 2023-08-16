package com.asfoundation.wallet.wallets.repository

import com.appcoins.wallet.core.network.backend.model.WalletInfoResponse
import com.appcoins.wallet.core.utils.android_common.BalanceUtils
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.TokenValue
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.balance.TokenBalance
import com.asfoundation.wallet.ui.iab.FiatValue
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asfoundation.wallet.wallets.domain.WalletBalance
import io.reactivex.Single
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject

class BalanceRepository @Inject constructor() {

  companion object {
    const val APPC_CURRENCY = "APPC_CURRENCY"
    const val APPC_C_CURRENCY = "APPC_C_CURRENCY"
    const val ETH_CURRENCY = "ETH_CURRENCY"

    const val FIAT_SCALE = 4
  }

  fun getWalletBalance(
    info: WalletInfoResponse
  ): Single<WalletBalance> {
    val credits = BalanceUtils.weiToEth(info.appcCreditsBalanceWei.toBigDecimal())
      .setScale(FIAT_SCALE, RoundingMode.FLOOR)
    val appc = BalanceUtils.weiToEth(info.appcBalanceWei.toBigDecimal())
      .setScale(FIAT_SCALE, RoundingMode.FLOOR)
    val eth = BalanceUtils.weiToEth(info.ethBalanceWei.toBigDecimal())
      .setScale(FIAT_SCALE, RoundingMode.FLOOR)

    return Single.just(
      mapToWalletBalance(
        credits, info.appcCreditsBalancFiat.setScale(FIAT_SCALE, RoundingMode.FLOOR),
        appc, info.appcBalanceFiat.setScale(FIAT_SCALE, RoundingMode.FLOOR),
        eth, info.ethBalanceFiat.setScale(FIAT_SCALE, RoundingMode.FLOOR),
        info.currency, info.symbol
      )
    )
  }

  fun mapToWalletBalance(
    creditsValue: BigDecimal, creditsFiatAmount: BigDecimal,
    appcValue: BigDecimal, appcFiatAmount: BigDecimal,
    ethValue: BigDecimal, ethFiatAmount: BigDecimal,
    fiatCurrency: String, fiatSymbol: String
  ): WalletBalance {
    val creditsToken =
      mapToTokenBalance(
        creditsValue, APPC_C_CURRENCY, WalletCurrency.CREDITS.symbol,
        FiatValue(creditsFiatAmount, fiatCurrency, fiatSymbol)
      )
    val appcToken =
      mapToTokenBalance(
        appcValue, APPC_CURRENCY, WalletCurrency.APPCOINS.symbol,
        FiatValue(appcFiatAmount, fiatCurrency, fiatSymbol)
      )
    val ethToken =
      mapToTokenBalance(
        ethValue, ETH_CURRENCY, WalletCurrency.ETHEREUM.symbol,
        FiatValue(ethFiatAmount, fiatCurrency, fiatSymbol)
      )
    val balance = getOverrallBalance(creditsToken, appcToken, ethToken)
    val creditsFiat = getCreditsFiatBalance(creditsToken, appcToken)
    return WalletBalance(balance, creditsFiat, creditsToken, appcToken, ethToken)

  }

  fun roundToEth(tokenAmount: BigInteger): BigDecimal {
    return BalanceUtils.weiToEth(tokenAmount.toBigDecimal())
      .setScale(FIAT_SCALE, RoundingMode.FLOOR)
  }

  private fun mapToTokenBalance(
    tokenValue: BigDecimal,
    tokenCurrency: String,
    tokenSymbol: String,
    fiatValue: FiatValue
  ): TokenBalance {
    return TokenBalance(TokenValue(tokenValue, tokenCurrency, tokenSymbol), fiatValue)
  }

  private fun getOverrallBalance(
    creditsBalance: TokenBalance, appcBalance: TokenBalance,
    ethBalance: TokenBalance
  ): FiatValue {
    var balance =
      getAddBalanceValue(BalanceInteractor.BIG_DECIMAL_MINUS_ONE, creditsBalance.fiat.amount)
    balance = getAddBalanceValue(balance, appcBalance.fiat.amount)
    balance = getAddBalanceValue(balance, ethBalance.fiat.amount)
    return FiatValue(balance, appcBalance.fiat.currency, appcBalance.fiat.symbol)
  }

  private fun getCreditsFiatBalance(
    creditsBalance: TokenBalance,
    appcBalance: TokenBalance
  ): FiatValue {
    val balance =
      getAddBalanceValue(BalanceInteractor.BIG_DECIMAL_MINUS_ONE, creditsBalance.fiat.amount)
    return FiatValue(balance, appcBalance.fiat.currency, appcBalance.fiat.symbol)
  }

  private fun getAddBalanceValue(currentValue: BigDecimal, value: BigDecimal): BigDecimal {
    return if (value.compareTo(BalanceInteractor.BIG_DECIMAL_MINUS_ONE) == 1) {
      if (currentValue.compareTo(BalanceInteractor.BIG_DECIMAL_MINUS_ONE) == 1) {
        currentValue.add(value)
      } else {
        value
      }
    } else {
      currentValue
    }
  }
}