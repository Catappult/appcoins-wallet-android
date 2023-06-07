package com.appcoins.wallet.feature.walletInfo.data.wallet

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class FindDefaultWalletInteract @Inject constructor(
    private val walletRepository: WalletRepositoryType,
    private val rxSchedulers: RxSchedulers
) {

  fun find(): Single<Wallet> {
    return walletRepository.getDefaultWallet()
        .subscribeOn(rxSchedulers.io)
        .onErrorResumeNext {
          walletRepository.fetchWallets()
              .filter { wallets -> wallets.isNotEmpty() }
              .map { wallets: Array<Wallet> ->
                wallets[0]
              }
              .flatMapCompletable { wallet: Wallet ->
                walletRepository.setDefaultWallet(wallet.address)
              }
              .andThen(walletRepository.getDefaultWallet())
        }
  }
}