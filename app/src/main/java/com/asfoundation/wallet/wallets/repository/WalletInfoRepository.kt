package com.asfoundation.wallet.wallets.repository

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

class WalletInfoRepository @Inject constructor(private val api: WalletInfoApi,
                                               private val walletInfoDao: WalletInfoDao,
                                               private val balanceRepository: BalanceRepository,
                                               private val rxSchedulers: RxSchedulers) {

  fun getLatestWalletInfo(walletAddress: String,
                          updateFiatValues: Boolean): Single<WalletInfo> {
    return updateWalletInfo(walletAddress, updateFiatValues)
        .andThen(observeWalletInfo(walletAddress).firstOrError())
  }

  /**
   * Note that this will fetch from network if no cached value exists, so it is not a true
   * cached value for every case.
   * If you always want to fetch from network, see [observeUpdatedWalletInfo]
   */
  fun getCachedWalletInfo(walletAddress: String): Single<WalletInfo> {
    return walletInfoDao.getWalletInfo(walletAddress)
        .flatMap { list ->
          if (list.isNotEmpty()) {
            return@flatMap Single.just(
                WalletInfo(list[0].wallet, getWalletBalance(list[0]), list[0].blocked,
                    list[0].verified, list[0].logging))
          }
          return@flatMap fetchWalletInfo(walletAddress, updateFiatValues = true)
              .map { entity ->
                return@map WalletInfo(entity.wallet, getWalletBalance(entity), entity.blocked,
                    entity.verified, entity.logging)
              }
        }
  }

  fun observeUpdatedWalletInfo(walletAddress: String,
                               updateFiatValues: Boolean): Observable<WalletInfo> {
    return Observable.merge(observeWalletInfo(walletAddress),
        updateWalletInfo(walletAddress, updateFiatValues).toObservable())
  }

  fun observeWalletInfo(walletAddress: String): Observable<WalletInfo> {
    return walletInfoDao.observeWalletInfo(walletAddress.normalize())
        .map { entity ->
          return@map WalletInfo(entity.wallet, getWalletBalance(entity), entity.blocked,
              entity.verified, entity.logging
          )
        }
        .doOnError { e -> e.printStackTrace() }
        .subscribeOn(rxSchedulers.io)
  }

  /**
   * Retrieves Wallet Info and fiat values (if specified), saving it to DB.
   * Fiat values is optional because it involves 3 requests, and it really only is useful in
   * My Wallets for now.
   */
  fun updateWalletInfo(walletAddress: String, updateFiatValues: Boolean): Completable {
    return fetchWalletInfo(walletAddress, updateFiatValues)
        .ignoreElement()
        .onErrorComplete()
        .subscribeOn(rxSchedulers.io)
  }

  private fun fetchWalletInfo(walletAddress: String,
                              updateFiatValues: Boolean): Single<WalletInfoEntity> {
    return api.getWalletInfo(walletAddress)
        .flatMap { walletInfoResponse ->
          if (updateFiatValues) {
            return@flatMap balanceRepository.getWalletBalance(
                walletInfoResponse.appcCreditsBalanceWei, walletInfoResponse.appcBalanceWei,
                walletInfoResponse.ethBalanceWei)
                .map { walletBalance ->
                  val fiat = walletBalance.creditsBalance.fiat
                  WalletInfoEntity(
                      walletInfoResponse.wallet.normalize(),
                      walletInfoResponse.appcCreditsBalanceWei,
                      walletInfoResponse.appcBalanceWei, walletInfoResponse.ethBalanceWei,
                      walletInfoResponse.blocked, walletInfoResponse.verified,
                      walletInfoResponse.logging, walletBalance.creditsBalance.fiat.amount,
                      walletBalance.appcBalance.fiat.amount, walletBalance.ethBalance.fiat.amount,
                      fiat.currency, fiat.symbol
                  )
                }
                .doOnSuccess { entity -> walletInfoDao.insertWalletInfoWithFiat(entity) }
          }
          return@flatMap Single.just(
              WalletInfoEntity(walletInfoResponse.wallet.normalize(),
                  walletInfoResponse.ethBalanceWei,
                  walletInfoResponse.appcBalanceWei, walletInfoResponse.appcCreditsBalanceWei,
                  walletInfoResponse.blocked, walletInfoResponse.verified,
                  walletInfoResponse.logging, null, null, null, null, null
              ))
              .doOnSuccess { entity -> walletInfoDao.insertOrUpdateNoFiat(entity) }
        }
        .doOnError { e -> e.printStackTrace() }
  }

  // This normalization is important as wallet addresses can be received with mixed case
  private fun String.normalize(): String {
    return this.toLowerCase(Locale.ROOT)
  }

  private fun getWalletBalance(entity: WalletInfoEntity): WalletBalance {
    val credits = balanceRepository.roundToEth(entity.appcCreditsBalanceWei)
    val creditsFiatAmount = entity.appcCreditsBalanceFiat ?: BigDecimal.ZERO
    val appc = balanceRepository.roundToEth(entity.appcBalanceWei)
    val appcFiatAmount = entity.appcBalanceFiat ?: BigDecimal.ZERO
    val eth = balanceRepository.roundToEth(entity.ethBalanceWei)
    val ethFiatAmount = entity.ethBalanceFiat ?: BigDecimal.ZERO
    return balanceRepository.mapToWalletBalance(credits, creditsFiatAmount, appc,
        appcFiatAmount, eth, ethFiatAmount, entity.fiatCurrency ?: "", entity.fiatSymbol ?: "")
  }

  interface WalletInfoApi {
    @GET("/transaction/wallet/{address}/info")
    fun getWalletInfo(@Path("address") address: String): Single<WalletInfoResponse>
  }
}