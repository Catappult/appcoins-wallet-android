package com.asfoundation.wallet.ui.balance

import android.util.Pair
import com.asfoundation.wallet.entity.Balance
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.GetDefaultWalletBalance
import com.asfoundation.wallet.service.LocalCurrencyConversionService
import com.asfoundation.wallet.ui.balance.database.BalanceDetailsDao
import com.asfoundation.wallet.ui.balance.database.BalanceDetailsEntity
import com.asfoundation.wallet.ui.balance.database.BalanceDetailsMapper
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable

class AppcoinsBalanceRepository(
    private val balanceGetter: GetDefaultWalletBalance,
    private val localCurrencyConversionService: LocalCurrencyConversionService,
    private val balanceDetailsDao: BalanceDetailsDao,
    private val balanceDetailsMapper: BalanceDetailsMapper,
    private val networkScheduler: Scheduler) :
    BalanceRepository {
  private var ethBalanceDisposable: Disposable? = null
  private var appcBalanceDisposable: Disposable? = null
  private var creditsBalanceDisposable: Disposable? = null

  companion object {
    private const val SUM_FIAT_SCALE = 4
  }

  override fun getEthBalance(wallet: Wallet): Observable<Pair<Balance, FiatValue>> {
    if (ethBalanceDisposable == null || ethBalanceDisposable!!.isDisposed) {
      ethBalanceDisposable = balanceGetter.getEthereumBalance(wallet)
          .observeOn(networkScheduler)
          .flatMapObservable { balance ->
            localCurrencyConversionService.getEtherToLocalFiat(balance.getStringValue(),
                SUM_FIAT_SCALE)
                .map { fiatValue ->
                  balanceDetailsDao.updateEthBalance(wallet.address, balance.getStringValue(),
                      fiatValue.amount.toString(), fiatValue.currency, fiatValue.symbol)
                }
          }
          .subscribe({}, { it.printStackTrace() })
    }
    return getBalance(wallet.address)
        .map { balanceDetailsMapper.mapEthBalance(it) }
  }

  override fun getAppcBalance(wallet: Wallet): Observable<Pair<Balance, FiatValue>> {
    if (appcBalanceDisposable == null || appcBalanceDisposable!!.isDisposed) {
      balanceGetter.getAppcBalance(wallet)
          .observeOn(networkScheduler)
          .flatMapObservable { balance ->
            localCurrencyConversionService.getAppcToLocalFiat(balance.getStringValue(),
                SUM_FIAT_SCALE)
                .map { fiatValue ->
                  balanceDetailsDao.updateAppcBalance(wallet.address, balance.getStringValue(),
                      fiatValue.amount.toString(), fiatValue.currency, fiatValue.symbol)
                }
          }
          .onExceptionResumeNext {}
          .subscribe()
    }
    return getBalance(wallet.address)
        .map { balanceDetailsMapper.mapAppcBalance(it) }
  }

  override fun getCreditsBalance(wallet: Wallet): Observable<Pair<Balance, FiatValue>> {
    if (creditsBalanceDisposable == null || creditsBalanceDisposable!!.isDisposed) {
      balanceGetter.getCredits(wallet)
          .observeOn(networkScheduler)
          .flatMapObservable { balance ->
            localCurrencyConversionService.getAppcToLocalFiat(balance.getStringValue(),
                SUM_FIAT_SCALE)
                .map { fiatValue ->
                  balanceDetailsDao.updateCreditsBalance(wallet.address, balance.getStringValue(),
                      fiatValue.amount.toString(), fiatValue.currency, fiatValue.symbol)
                }
          }
          .onExceptionResumeNext {}
          .subscribe()
    }
    return getBalance(wallet.address)
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
}