package com.asfoundation.wallet.wallets.repository

import com.asfoundation.wallet.analytics.SentryEventLogger
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.wallets.db.WalletInfoDao
import com.asfoundation.wallet.wallets.db.entity.WalletInfoEntity
import com.asfoundation.wallet.wallets.domain.WalletBalance
import com.asfoundation.wallet.wallets.domain.WalletInfo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class WalletInfoRepository @Inject constructor(
  private val api: WalletInfoApi,
  private val walletInfoDao: WalletInfoDao,
  private val balanceRepository: BalanceRepository,
  private val sentryEventLogger: SentryEventLogger,
  private val rxSchedulers: RxSchedulers
) {

  fun getLatestWalletInfo(walletAddress: String, updateFiatValues: Boolean): Single<WalletInfo> =
    updateWalletInfo(walletAddress, updateFiatValues)
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
          Single.just(
            WalletInfo(
              wallet = list[0].wallet,
              walletBalance = getWalletBalance(list[0]),
              blocked = list[0].blocked,
              verified = list[0].verified,
              logging = list[0].logging,
              hasBackup = list[0].hasBackup
            )
          )
        } else {
          fetchWalletInfo(walletAddress, updateFiatValues = true)
            .map {
              WalletInfo(
                wallet = it.wallet,
                walletBalance = getWalletBalance(it),
                blocked = it.blocked,
                verified = it.verified,
                logging = it.logging,
                hasBackup = list[0].hasBackup
              )
            }
        }
      }

  fun observeUpdatedWalletInfo(
    walletAddress: String,
    updateFiatValues: Boolean
  ): Observable<WalletInfo> = Observable.merge(
    observeWalletInfo(walletAddress),
    updateWalletInfo(walletAddress, updateFiatValues).toObservable()
  )

  fun observeWalletInfo(walletAddress: String): Observable<WalletInfo> =
    walletInfoDao.observeWalletInfo(walletAddress.normalize())
      .map {
        WalletInfo(
          wallet = it.wallet,
          walletBalance = getWalletBalance(it),
          blocked = it.blocked,
          verified = it.verified,
          logging = it.logging,
          hasBackup = it.hasBackup
        )
      }
      .doOnError(Throwable::printStackTrace)
      .subscribeOn(rxSchedulers.io)

  /**
   * Retrieves Wallet Info and fiat values (if specified), saving it to DB.
   * Fiat values is optional because it involves 3 requests, and it really only is useful in
   * My Wallets for now.
   */
  fun updateWalletInfo(walletAddress: String, updateFiatValues: Boolean): Completable =
    fetchWalletInfo(walletAddress, updateFiatValues)
      .ignoreElement()
      .onErrorComplete()
      .subscribeOn(rxSchedulers.io)

  private fun fetchWalletInfo(
    walletAddress: String,
    updateFiatValues: Boolean
  ): Single<WalletInfoEntity> = api.getWalletInfo(walletAddress)
    .flatMap { walletInfoResponse ->
      sentryEventLogger.enabled.set(walletInfoResponse.breadcrumbs == 1)
      if (updateFiatValues) {
        balanceRepository.getWalletBalance(
          walletInfoResponse.appcCreditsBalanceWei,
          walletInfoResponse.appcBalanceWei,
          walletInfoResponse.ethBalanceWei
        )
          .map { walletBalance ->
            val fiat = walletBalance.creditsBalance.fiat
            WalletInfoEntity(
              wallet = walletInfoResponse.wallet.normalize(),
              appcCreditsBalanceWei = walletInfoResponse.appcCreditsBalanceWei,
              appcBalanceWei = walletInfoResponse.appcBalanceWei,
              ethBalanceWei = walletInfoResponse.ethBalanceWei,
              blocked = walletInfoResponse.blocked,
              verified = walletInfoResponse.verified,
              logging = walletInfoResponse.logging,
              hasBackup = walletInfoResponse.hasBackup,
              appcCreditsBalanceFiat = walletBalance.creditsBalance.fiat.amount,
              appcBalanceFiat = walletBalance.appcBalance.fiat.amount,
              ethBalanceFiat = walletBalance.ethBalance.fiat.amount,
              fiatCurrency = fiat.currency,
              fiatSymbol = fiat.symbol
            )
          }
          .doOnSuccess(walletInfoDao::insertWalletInfoWithFiat)
      } else {
        Single.just(
          WalletInfoEntity(
            wallet = walletInfoResponse.wallet.normalize(),
            appcCreditsBalanceWei = walletInfoResponse.ethBalanceWei,
            appcBalanceWei = walletInfoResponse.appcBalanceWei,
            ethBalanceWei = walletInfoResponse.appcCreditsBalanceWei,
            blocked = walletInfoResponse.blocked,
            verified = walletInfoResponse.verified,
            logging = walletInfoResponse.logging,
            hasBackup = walletInfoResponse.hasBackup,
            appcCreditsBalanceFiat = null,
            appcBalanceFiat = null,
            ethBalanceFiat = null,
            fiatCurrency = null,
            fiatSymbol = null
          )
        )
          .doOnSuccess(walletInfoDao::insertOrUpdateNoFiat)
      }
    }
    .doOnError(Throwable::printStackTrace)

  // This normalization is important as wallet addresses can be received with mixed case
  private fun String.normalize(): String = this.lowercase(Locale.ROOT)

  private fun getWalletBalance(entity: WalletInfoEntity): WalletBalance =
    balanceRepository.mapToWalletBalance(
      creditsValue = balanceRepository.roundToEth(entity.appcCreditsBalanceWei),
      creditsFiatAmount = entity.appcCreditsBalanceFiat ?: BigDecimal.ZERO,
      appcValue = balanceRepository.roundToEth(entity.appcBalanceWei),
      appcFiatAmount = entity.appcBalanceFiat ?: BigDecimal.ZERO,
      ethValue = balanceRepository.roundToEth(entity.ethBalanceWei),
      ethFiatAmount = entity.ethBalanceFiat ?: BigDecimal.ZERO,
      fiatCurrency = entity.fiatCurrency ?: "",
      fiatSymbol = entity.fiatSymbol ?: ""
    )

  interface WalletInfoApi {
    @GET("/transaction/wallet/{address}/info")
    fun getWalletInfo(@Path("address") address: String): Single<WalletInfoResponse>
  }
}