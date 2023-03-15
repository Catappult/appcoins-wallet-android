package com.asfoundation.wallet.wallets

import com.appcoins.wallet.core.utils.common.RxSchedulers
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.WalletRepositoryType
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