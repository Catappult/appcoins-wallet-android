package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.WalletRepositoryType
import io.reactivex.Scheduler
import io.reactivex.Single

class FindDefaultWalletInteract(private val walletRepository: WalletRepositoryType,
                                private val scheduler: Scheduler) {
  fun find(): Single<Wallet> {
    return walletRepository.defaultWallet.subscribeOn(scheduler)
        .onErrorResumeNext {
          walletRepository.fetchWallets()
              .filter { wallets: Array<Wallet?> -> wallets.isNotEmpty() }
              .map { wallets: Array<Wallet> ->
                wallets[0]
              }
              .flatMapCompletable { wallet: Wallet ->
                walletRepository.setDefaultWallet(wallet.address)
              }
              .andThen(walletRepository.defaultWallet)
        }
  }

}