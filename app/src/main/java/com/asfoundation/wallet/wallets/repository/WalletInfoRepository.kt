package com.asfoundation.wallet.wallets.repository

import com.appcoins.wallet.core.network.backend.api.WalletInfoApi
import com.appcoins.wallet.core.network.backend.model.WalletInfoResponse
import com.appcoins.wallet.core.analytics.analytics.SentryEventLogger
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.change_currency.use_cases.GetSelectedCurrencyUseCase
import com.asfoundation.wallet.wallets.db.WalletInfoDao
import com.asfoundation.wallet.wallets.db.entity.WalletInfoEntity
import com.asfoundation.wallet.wallets.domain.WalletBalance
import com.asfoundation.wallet.wallets.domain.WalletInfo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

class WalletInfoRepository @Inject constructor(
  private val api: WalletInfoApi,
  private val walletInfoDao: WalletInfoDao,
  private val balanceRepository: BalanceRepository,
  private val sentryEventLogger: SentryEventLogger,
  private val getSelectedCurrencyUseCase: GetSelectedCurrencyUseCase,
  private val rxSchedulers: RxSchedulers
) {

  fun getLatestWalletInfo(walletAddress: String): Single<WalletInfo> =
    updateWalletInfo(walletAddress)
      .andThen(observeWalletInfo(walletAddress).firstOrError())

  /**
   * Note that this will fetch from network if no cached value exists, so it is not a true
   * cached value for every case.
   * If you always want to fetch from network, see [observeUpdatedWalletInfo]
   */
  fun getCachedWalletInfo(walletAddress: String): Single<WalletInfo> =
    walletInfoDao.getWalletInfo(walletAddress)
      .flatMap { list ->
        if (list.isNotEmpty()) {
          Single.just(list[0].toWalletInfo())
        } else {
          fetchWalletInfo(walletAddress)
            .map { it.toWalletInfo() }
        }
      }

  fun observeUpdatedWalletInfo(
    walletAddress: String
  ): Observable<WalletInfo> = Observable.merge(
    observeWalletInfo(walletAddress),
    updateWalletInfo(walletAddress).toObservable()
  )

  fun observeWalletInfo(walletAddress: String): Observable<WalletInfo> =
    walletInfoDao.observeWalletInfo(walletAddress.normalize())
      .map { it.toWalletInfo() }
      .doOnError(Throwable::printStackTrace)
      .subscribeOn(rxSchedulers.io)

  /**
   * Retrieves Wallet Info and fiat values, saving it to DB.
   */
  fun updateWalletInfo(walletAddress: String): Completable =
    fetchWalletInfo(walletAddress)
      .ignoreElement()
      .onErrorComplete()
      .subscribeOn(rxSchedulers.io)

  fun updateWalletName(walletAddress: String, name: String?): Completable =
    Completable.fromAction {
      walletInfoDao.insertOrUpdateName(
        WalletInfoEntity(
          wallet = walletAddress.normalize(),
          name = name,
          appcCreditsBalanceWei = BigInteger.valueOf(0),
          appcBalanceWei = BigInteger.valueOf(0),
          ethBalanceWei = BigInteger.valueOf(0),
          blocked = false,
          verified = false,
          logging = false,
          hasBackup = 0,
          appcCreditsBalanceFiat = null,
          appcBalanceFiat = null,
          ethBalanceFiat = null,
          fiatCurrency = null,
          fiatSymbol = null
        )
      )
    }.subscribeOn(rxSchedulers.io)
      .doOnError(Throwable::printStackTrace)
      .onErrorComplete()

  fun deleteWalletInfo(walletAddress: String): Completable =
    Completable.fromAction { walletInfoDao.deleteByAddress(walletAddress) }
      .onErrorComplete()
      .subscribeOn(rxSchedulers.io)

  private fun fetchWalletInfo(
    walletAddress: String
  ): Single<WalletInfoEntity> {
    return getSelectedCurrencyUseCase(bypass = false).flatMap { currency ->
      api.getWalletInfo(walletAddress, currency)
      .flatMap { walletInfoResponse ->
        sentryEventLogger.enabled.set(walletInfoResponse.breadcrumbs == 1)
        balanceRepository.getWalletBalance(
          walletInfoResponse
        )
          .map { walletInfoResponse.toWalletInfoEntity(it) }
          .doOnSuccess(walletInfoDao::insertOrUpdateWithFiat)
      }
      .doOnError(Throwable::printStackTrace)
    }
  }

  // This normalization is important as wallet addresses can be received with mixed case
  private fun String.normalize(): String = this.lowercase(Locale.ROOT)

  private fun WalletInfoEntity.toWalletInfo() =
    WalletInfo(
      wallet = wallet,
      name = name?.ifEmpty { null } ?: wallet.replaceRange(IntRange(6, wallet.length - 5), " ··· "),
      walletBalance = balanceRepository.mapToWalletBalance(
        creditsValue = balanceRepository.roundToEth(appcCreditsBalanceWei),
        creditsFiatAmount = appcCreditsBalanceFiat ?: BigDecimal.ZERO,
        appcValue = balanceRepository.roundToEth(appcBalanceWei),
        appcFiatAmount = appcBalanceFiat ?: BigDecimal.ZERO,
        ethValue = balanceRepository.roundToEth(ethBalanceWei),
        ethFiatAmount = ethBalanceFiat ?: BigDecimal.ZERO,
        fiatCurrency = fiatCurrency ?: "",
        fiatSymbol = fiatSymbol ?: ""
      ),
      blocked = blocked,
      verified = verified,
      logging = logging,
      backupDate = hasBackup
    )

  private fun WalletInfoResponse.toWalletInfoEntity(walletBalance: WalletBalance? = null) =
    WalletInfoEntity(
      wallet = wallet.normalize(),
      name = null,
      appcCreditsBalanceWei = appcCreditsBalanceWei,
      appcBalanceWei = appcBalanceWei,
      ethBalanceWei = ethBalanceWei,
      blocked = blocked,
      verified = verified,
      logging = logging,
      hasBackup = syntheticBackupDate,
      appcCreditsBalanceFiat = walletBalance?.creditsBalance?.fiat?.amount,
      appcBalanceFiat = walletBalance?.appcBalance?.fiat?.amount,
      ethBalanceFiat = walletBalance?.ethBalance?.fiat?.amount,
      fiatCurrency = walletBalance?.creditsBalance?.fiat?.currency,
      fiatSymbol = walletBalance?.creditsBalance?.fiat?.symbol
    )
}