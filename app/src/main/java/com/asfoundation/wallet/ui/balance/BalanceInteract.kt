package com.asfoundation.wallet.ui.balance

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.ui.TokenValue
import com.asfoundation.wallet.ui.balance.BalanceFragmentPresenter.Companion.APPC_CURRENCY
import com.asfoundation.wallet.ui.balance.BalanceFragmentPresenter.Companion.APPC_C_CURRENCY
import com.asfoundation.wallet.ui.balance.BalanceFragmentPresenter.Companion.ETH_CURRENCY
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.annotations.Nullable
import io.reactivex.functions.Function3
import java.math.BigDecimal

class BalanceInteract(
    private val walletInteract: FindDefaultWalletInteract,
    private val balanceRepository: BalanceRepository,
    private val preferencesRepositoryType: PreferencesRepositoryType,
    private val smsValidationInteract: SmsValidationInteract) {

  fun getAppcBalance(): Observable<Pair<Balance, FiatValue>> {
    return walletInteract.find()
        .flatMapObservable { balanceRepository.getAppcBalance(it.address) }
  }

  fun getEthBalance(): Observable<Pair<Balance, FiatValue>> {
    return walletInteract.find()
        .flatMapObservable { balanceRepository.getEthBalance(it.address) }
  }

  fun getCreditsBalance(): Observable<Pair<Balance, FiatValue>> {
    return walletInteract.find()
        .flatMapObservable { balanceRepository.getCreditsBalance(it.address) }
  }

  private fun getStoredAppcBalance(walletAddress: String?): Single<Pair<Balance, FiatValue>> {
    return (walletAddress?.let { Single.just(it) } ?: walletInteract.find()
        .map { it.address })
        .flatMap { balanceRepository.getStoredAppcBalance(it) }
  }

  private fun getStoredEthBalance(walletAddress: String?): Single<Pair<Balance, FiatValue>> {
    return (walletAddress?.let { Single.just(it) } ?: walletInteract.find()
        .map { it.address })
        .flatMap { balanceRepository.getStoredEthBalance(it) }
  }

  private fun getStoredCreditsBalance(walletAddress: String?): Single<Pair<Balance, FiatValue>> {
    return (walletAddress?.let { Single.just(it) } ?: walletInteract.find()
        .map { it.address })
        .flatMap { balanceRepository.getStoredCreditsBalance(it) }
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

  fun getTotalBalance(address: String): Observable<FiatValue> {
    return Observable.zip(
        balanceRepository.getCreditsBalance(address),
        balanceRepository.getAppcBalance(address),
        balanceRepository.getEthBalance(address),
        Function3 { creditsBalance, appcBalance, ethBalance ->
          getOverallBalance(mapToBalance(creditsBalance, APPC_C_CURRENCY),
              mapToBalance(appcBalance, APPC_CURRENCY), mapToBalance(ethBalance, ETH_CURRENCY))
        })
  }

  fun requestActiveWalletAddress(): Single<String> {
    return walletInteract.find()
        .map { it.address }
  }

  fun getStoredOverallBalance(@Nullable walletAddress: String? = null): Single<FiatValue> {
    return Single.zip(
        getStoredAppcBalance(walletAddress),
        getStoredEthBalance(walletAddress),
        getStoredCreditsBalance(walletAddress),
        Function3 { creditsBalance, appcBalance, ethBalance ->
          mapOverallBalance(creditsBalance, appcBalance, ethBalance)
        }
    )
  }

  fun getStoredBalanceScreenModel(walletAddress: String): Single<BalanceScreenModel> {
    return Single.zip(
        getStoredAppcBalance(walletAddress),
        getStoredEthBalance(walletAddress),
        getStoredCreditsBalance(walletAddress),
        Function3 { appcBalance, ethBalance, creditsBalance ->
          mapToBalanceScreenModel(creditsBalance, appcBalance, ethBalance)
        }
    )
  }

  fun isWalletValid(): Single<Pair<String, WalletValidationStatus>> {
    return walletInteract.find()
        .flatMap { wallet ->
          smsValidationInteract.getValidationStatus(wallet.address)
              .map { Pair(wallet.address, it) }
        }
  }

  fun isWalletValidated(address: String) = preferencesRepositoryType.isWalletValidated(address)

  fun hasSeenBackupTooltip() = preferencesRepositoryType.getSeenBackupTooltip()

  fun saveSeenBackupTooltip() = preferencesRepositoryType.saveSeenBackupTooltip()

  private fun mapOverallBalance(creditsBalance: Pair<Balance, FiatValue>,
                                appcBalance: Pair<Balance, FiatValue>,
                                ethBalance: Pair<Balance, FiatValue>): FiatValue {
    var balance = getAddBalanceValue(BalanceFragmentPresenter.BIG_DECIMAL_MINUS_ONE,
        creditsBalance.second.amount)
    balance = getAddBalanceValue(balance, appcBalance.second.amount)
    balance = getAddBalanceValue(balance, ethBalance.second.amount)

    return FiatValue(balance, appcBalance.second.currency, appcBalance.second.symbol)

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
