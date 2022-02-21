package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.WalletRepositoryType
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ObserveDefaultWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepositoryType) {

  operator fun invoke(): Observable<Wallet> {
    return walletRepository.observeDefaultWallet()
        .subscribeOn(Schedulers.io())
        .onErrorResumeNext { _: Throwable ->
          return@onErrorResumeNext walletRepository.fetchWallets()
              .filter { wallets -> wallets.isNotEmpty() }
              .map { wallets: Array<Wallet> ->
                wallets[0]
              }
              .flatMapCompletable { wallet: Wallet ->
                walletRepository.setDefaultWallet(wallet.address)
              }
              .andThen(walletRepository.observeDefaultWallet())
        }
  }
}