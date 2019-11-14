package com.asfoundation.wallet.ui.balance

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.ui.TokenValue
import com.asfoundation.wallet.ui.balance.BalanceFragmentPresenter.Companion.APPC_CURRENCY
import com.asfoundation.wallet.ui.balance.BalanceFragmentPresenter.Companion.APPC_C_CURRENCY
import com.asfoundation.wallet.ui.balance.BalanceFragmentPresenter.Companion.ETH_CURRENCY
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import io.reactivex.functions.Function3
import java.math.BigDecimal

class BalanceInteract(
    private val walletInteract: FindDefaultWalletInteract,
    private val balanceRepository: BalanceRepository) {

  fun getAppcBalance(): Observable<Pair<Balance, FiatValue>> {
    return walletInteract.find()
        .flatMapObservable { balanceRepository.getAppcBalance(it) }
  }

  fun getEthBalance(): Observable<Pair<Balance, FiatValue>> {
    return walletInteract.find()
        .flatMapObservable { balanceRepository.getEthBalance(it) }
  }

  fun getCreditsBalance(): Observable<Pair<Balance, FiatValue>> {
    return walletInteract.find()
        .flatMapObservable { balanceRepository.getCreditsBalance(it) }
  }

  fun requestTokenConversion(): Observable<BalanceScreenModel> {
    return Observable.zip(
        getCreditsBalance(),
        getAppcBalance(),
        getEthBalance(),
        Function3 { creditsBalance, appcBalance, ethBalance ->
          mapToBalanceScreenModel(creditsBalance, appcBalance, ethBalance)
        }
    )
  }

  private fun mapToBalanceScreenModel(creditsBalance: Pair<Balance, FiatValue>,
                                      appcBalance: Pair<Balance, FiatValue>,
                                      ethBalance: Pair<Balance, FiatValue>): BalanceScreenModel {
    val credits = mapToBalance(creditsBalance, APPC_C_CURRENCY)
    val appc = mapToBalance(appcBalance, APPC_CURRENCY)
    val eth = mapToBalance(ethBalance, ETH_CURRENCY)
    val overall = getOverallBalance(credits, appc, eth)
    return BalanceScreenModel(overall, credits, appc, eth)
  }

  private fun mapToBalance(pair: Pair<Balance, FiatValue>, currency: String): TokenBalance {
    return TokenBalance(TokenValue(pair.first.value, currency, pair.first.symbol), pair.second)
  }

  private fun getOverallBalance(creditsBalance: TokenBalance,
                                appcBalance: TokenBalance,
                                ethBalance: TokenBalance): FiatValue {
    var balance = getAddBalanceValue(BalanceFragmentPresenter.BIG_DECIMAL_MINUS_ONE,
        creditsBalance.fiat.amount)
    balance = getAddBalanceValue(balance, appcBalance.fiat.amount)
    balance = getAddBalanceValue(balance, ethBalance.fiat.amount)

    if (balance.compareTo(BalanceFragmentPresenter.BIG_DECIMAL_MINUS_ONE) == 1) {
      balance.stripTrailingZeros()
          .setScale(2, BigDecimal.ROUND_DOWN)
    }

    return FiatValue(balance, appcBalance.fiat.currency, appcBalance.fiat.symbol)
  }

  private fun getAddBalanceValue(currentValue: BigDecimal, value: BigDecimal): BigDecimal {
    return if (value.compareTo(BalanceFragmentPresenter.BIG_DECIMAL_MINUS_ONE) == 1) {
      if (currentValue.compareTo(BalanceFragmentPresenter.BIG_DECIMAL_MINUS_ONE) == 1) {
        currentValue.add(value)
      } else {
        value
      }
    } else {
      currentValue
    }
  }

}
