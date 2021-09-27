package com.asfoundation.wallet.ui.balance

import android.util.Pair
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.balance.database.BalanceDetailsDao
import com.asfoundation.wallet.ui.balance.database.BalanceDetailsEntity
import com.asfoundation.wallet.ui.balance.database.BalanceDetailsMapper
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.wallets.GetDefaultWalletBalanceInteract
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable

class AppcoinsBalanceRepository(private val balanceGetter: GetDefaultWalletBalanceInteract,
                                private val localCurrencyConversionService: LocalCurrencyConversionService,
                                private val balanceDetailsDao: BalanceDetailsDao,
                                private val balanceDetailsMapper: BalanceDetailsMapper,
                                private val networkScheduler: Scheduler,
                                private val getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase) :
    BalanceRepository {
  private var ethBalanceDisposable: Disposable? = null
  private var appcBalanceDisposable: Disposable? = null
  private var creditsBalanceDisposable: Disposable? = null

  companion object {
    private const val SUM_FIAT_SCALE = 4
  }

  override fun getEthBalance(address: String): Observable<Pair<Balance, FiatValue>> {
    if (ethBalanceDisposable == null || ethBalanceDisposable!!.isDisposed) {
      ethBalanceDisposable = balanceGetter.getEthereumBalance(address)
          .observeOn(networkScheduler)
          .flatMapObservable { balance ->
            getSelectedCurrencyUseCase()
                .observeOn(networkScheduler)
                .flatMapObservable { targetCurrency ->
                  localCurrencyConversionService.getValueToFiat(balance.getStringValue(), "ETH",
                      targetCurrency, SUM_FIAT_SCALE)
                      .map { fiatValue ->
                        balanceDetailsDao.updateEthBalance(address, balance.getStringValue(),
                            fiatValue.amount.toString(), fiatValue.currency, fiatValue.symbol)
                      }
                }

          }
          .subscribe({}, { it.printStackTrace() })
    }
    return getBalance(address)
        .map { balanceDetailsMapper.mapEthBalance(it) }
  }

  override fun getAppcBalance(address: String): Observable<Pair<Balance, FiatValue>> {
    if (appcBalanceDisposable == null || appcBalanceDisposable!!.isDisposed) {
      balanceGetter.getAppcBalance(address)
          .observeOn(networkScheduler)
          .flatMapObservable { balance ->
            getSelectedCurrencyUseCase()
                .observeOn(networkScheduler)
                .flatMapObservable { targetCurrency ->
                  localCurrencyConversionService.getValueToFiat(balance.getStringValue(), "APPC",
                      targetCurrency, SUM_FIAT_SCALE)
                      .map { fiatValue ->
                        balanceDetailsDao.updateAppcBalance(address, balance.getStringValue(),
                            fiatValue.amount.toString(), fiatValue.currency, fiatValue.symbol)
                      }
                }
          }
          .onExceptionResumeNext {}
          .subscribe()
    }
    return getBalance(address)
        .map { balanceDetailsMapper.mapAppcBalance(it) }
  }

  override fun getCreditsBalance(address: String): Observable<Pair<Balance, FiatValue>> {
    if (creditsBalanceDisposable == null || creditsBalanceDisposable!!.isDisposed) {
      balanceGetter.getCredits(address)
          .observeOn(networkScheduler)
          .flatMapObservable { balance ->
            getSelectedCurrencyUseCase()
                .observeOn(networkScheduler)
                .flatMapObservable { targetCurrency ->
                  localCurrencyConversionService.getValueToFiat(balance.getStringValue(), "APPC",
                      targetCurrency, SUM_FIAT_SCALE)
                      .map { fiatValue ->
                        balanceDetailsDao.updateCreditsBalance(address, balance.getStringValue(),
                            fiatValue.amount.toString(), fiatValue.currency, fiatValue.symbol)
                      }
                }
          }
          .onExceptionResumeNext {}
          .subscribe()
    }
    return getBalance(address)
        .map { balanceDetailsMapper.mapCreditsBalance(it) }
  }

  private fun getBalance(walletAddress: String): Observable<BalanceDetailsEntity?> {
    checkIfExistsOrCreate(walletAddress)
    return balanceDetailsDao.getBalance(walletAddress)
  }

  @Synchronized
  private fun checkIfExistsOrCreate(walletAddress: String) {
    val entity = balanceDetailsDao.getSyncBalance(walletAddress)
    if (entity == null) {
      balanceDetailsDao.insert(balanceDetailsMapper.map(walletAddress))
    }
  }

  override fun getStoredEthBalance(walletAddress: String): Single<Pair<Balance, FiatValue>> {
    return getBalance(walletAddress)
        .map { balanceDetailsMapper.mapEthBalance(it) }
        .firstOrError()
  }

  override fun getStoredAppcBalance(walletAddress: String): Single<Pair<Balance, FiatValue>> {
    return getBalance(walletAddress)
        .map { balanceDetailsMapper.mapAppcBalance(it) }
        .firstOrError()
  }

  override fun getStoredCreditsBalance(walletAddress: String): Single<Pair<Balance, FiatValue>> {
    return getBalance(walletAddress)
        .map { balanceDetailsMapper.mapCreditsBalance(it) }
        .firstOrError()
  }
}