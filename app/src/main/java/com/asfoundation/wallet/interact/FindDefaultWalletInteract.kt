package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class FindDefaultWalletInteract(private val walletRepository: WalletRepositoryType) {
  fun find(): Single<Wallet> {
    return walletRepository.defaultWallet.subscribeOn(Schedulers.io())
        .onErrorResumeNext {
          walletRepository.fetchWallets()
              .filter { wallets: Array<Wallet?> -> wallets.isNotEmpty() }
              .map { wallets: Array<Wallet> ->
                wallets[0]
              }
              .flatMapCompletable { wallet: Wallet ->
                walletRepository.setDefaultWallet(wallet.address)
              }
              .andThen(
                  walletRepository.defaultWallet)
        }
  }

}