package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletInfoRepository
import io.reactivex.Single
import javax.inject.Inject

class GetWalletInfoUseCase @Inject constructor(
    private val walletInfoRepository: WalletInfoRepository,
    private val getCurrentWalletUseCase: GetCurrentWalletUseCase) {

  /**
   * Retrieves WalletInfo
   *
   * @param address Wallet address, or null to use the currently active wallet
   * @param cached true to return the cached WalletInfo, or false if it should retrieve from network
   * @param updateFiat true if it should also update fiat, or false if not necessary
   */
  operator fun invoke(address: String?, cached: Boolean,
                      updateFiat: Boolean): Single<WalletInfo> {
    val walletAddressSingle =
        address?.let { Single.just(Wallet(address)) } ?: getCurrentWalletUseCase()
    return if (cached) {
      walletAddressSingle.flatMap {
        walletInfoRepository.getCachedWalletInfo(it.address)
      }
    } else {
      walletAddressSingle.flatMap {
        walletInfoRepository.getLatestWalletInfo(it.address, updateFiat)
      }
    }
  }
}