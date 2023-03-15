package com.asfoundation.wallet.wallets.repository

import com.appcoins.wallet.core.utils.common.BalanceUtils
import com.appcoins.wallet.ui.arch.RxSchedulers
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.TokenValue
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.balance.TokenBalance
import com.asfoundation.wallet.ui.iab.FiatValue
import com.appcoins.wallet.core.utils.common.WalletCurrency
import com.asfoundation.wallet.wallets.domain.WalletBalance
import io.reactivex.Single
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject

class BalanceRepository @Inject constructor(
  private val getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase,
  private val localCurrencyConversionService: LocalCurrencyConversionService,
  private val rxSchedulers: RxSchedulers
) {

  companion object {
    const val APPC_CURRENCY = "APPC_CURRENCY"
    const val APPC_C_CURRENCY = "APPC_C_CURRENCY"
    const val ETH_CURRENCY = "ETH_CURRENCY"

    const val FIAT_SCALE = 4
  }

  fun getWalletBalance(
    appcCreditsWei: BigInteger,
    appcWei: BigInteger,
    ethWei: BigInteger
  ): Single<WalletBalance> {
    val credits = BalanceUtils.weiToEth(appcCreditsWei.toBigDecimal())
      .setScale(FIAT_SCALE, RoundingMode.FLOOR)
    val appc = BalanceUtils.weiToEth(appcWei.toBigDecimal())
      .setScale(FIAT_SCALE, RoundingMode.FLOOR)
    val eth = BalanceUtils.weiToEth(ethWei.toBigDecimal())
      .setScale(FIAT_SCALE, RoundingMode.FLOOR)

    return getSelectedCurrencyUseCase(bypass = false)
      .flatMap { targetCurrency ->
        Single.zip(
          localCurrencyConversionService.getValueToFiat(
            credits.toString(), "APPC",
            targetCurrency, FIAT_SCALE
          )
            .subscribeOn(rxSchedulers.io),
          localCurrencyConversionService.getValueToFiat(
            appc.toString(), "APPC", targetCurrency,
            FIAT_SCALE
          )
            .subscribeOn(rxSchedulers.io),
          localCurrencyConversionService.getValueToFiat(
            eth.toString(), "ETH", targetCurrency,
            FIAT_SCALE
          )
            .subscribeOn(rxSchedulers.io),
          { creditsFiat, appcFiat, ethFiat ->
            return@zip mapToWalletBalance(
              credits, creditsFiat.amount, appc, appcFiat.amount,
              eth, ethFiat.amount, creditsFiat.currency, creditsFiat.symbol
            )
          }
        )
      }
      .subscribeOn(rxSchedulers.io)
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