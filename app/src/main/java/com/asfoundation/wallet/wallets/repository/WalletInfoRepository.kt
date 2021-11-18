package com.asfoundation.wallet.wallets.repository

import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.wallets.db.WalletInfoDao
import com.asfoundation.wallet.wallets.db.entity.WalletInfoEntity
import com.asfoundation.wallet.wallets.domain.WalletInfo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

class WalletInfoRepository(private val api: WalletInfoApi, private val db: WalletInfoDao,
                           private val rxSchedulers: RxSchedulers) {

  fun observeWalletInfo(walletAddress: String): Observable<WalletInfo> {
    return db.observeWalletInfo(walletAddress)
        .map { list ->
          if (list.isNotEmpty()) {
            return@map WalletInfo(list[0].wallet, list[0].ethBalanceWei, list[0].appcBalanceWei,
                list[0].appcCreditsBalanceWei, list[0].blocked, list[0].verified, list[0].logging)
          }
          throw UnknownError("No wallet info for the specified wallet address (${walletAddress})")
        }
        .subscribeOn(rxSchedulers.io)
  }

  fun updateWalletInfo(walletAddress: String): Completable {
    return api.getWalletInfo(walletAddress)
        .map { response ->
          WalletInfoEntity(response.wallet, response.ethBalanceWei, response.appcBalanceWei,
              response.appcCreditsBalanceWei, response.blocked, response.verified, response.logging)
        }
        .doOnSuccess { entity -> db.insertWalletInfo(entity) }
        .ignoreElement()
        .subscribeOn(rxSchedulers.io)
  }

  interface WalletInfoApi {
    @GET("/transaction/wallet/{address}/info")
    fun getWalletInfo(@Path("address") address: String): Single<WalletInfoResponse>
  }
}