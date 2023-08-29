package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletInfoRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class UpdateWalletInfoUseCase @Inject constructor(
    private val walletInfoRepository: WalletInfoRepository,
    private val getCurrentWalletUseCase: GetCurrentWalletUseCase) {

  /**
   * Updates WalletInfo
   *
   * @param address Wallet address, or null to use the currently active wallet
   */
  operator fun invoke(address: String?): Completable {
    val walletAddressSingle =
        address?.let { Single.just(Wallet(address)) } ?: getCurrentWalletUseCase()
    return walletAddressSingle.flatMapCompletable {
      walletInfoRepository.updateWalletInfo(it.address)
    }
  }
}