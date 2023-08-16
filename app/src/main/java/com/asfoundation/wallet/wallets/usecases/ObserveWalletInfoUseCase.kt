package com.asfoundation.wallet.wallets.usecases

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ObserveWalletInfoUseCase @Inject constructor(
    private val walletInfoRepository: WalletInfoRepository,
    private val getCurrentWalletUseCase: GetCurrentWalletUseCase) {

  /**
   * Observes WalletInfo
   *
   * @param address Wallet address, or null to use the currently active wallet
   * @param update true to also update WalletInfo, or false if it not necessary
   */
  operator fun invoke(address: String?, update: Boolean): Observable<WalletInfo> {
    val walletAddressSingle =
        address?.let { Single.just(Wallet(address)) } ?: getCurrentWalletUseCase()
    return if (update) {
      walletAddressSingle.flatMapObservable {
        walletInfoRepository.observeUpdatedWalletInfo(it.address)
      }
    } else {
      walletAddressSingle.flatMapObservable {
        walletInfoRepository.observeWalletInfo(it.address)
      }
    }
  }
}