package com.asfoundation.wallet.wallets

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.WalletRepositoryType
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single

class FindDefaultWalletInteract(private val walletRepository: WalletRepositoryType,
                                private val scheduler: Scheduler) {
  fun find(): Single<Wallet> {
    return walletRepository.getDefaultWallet()
        .subscribeOn(scheduler)
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