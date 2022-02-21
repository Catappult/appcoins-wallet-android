package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FindDefaultWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepositoryType) {

  operator fun invoke(): Single<Wallet> {
    return walletRepository.getDefaultWallet()
        .subscribeOn(Schedulers.io())
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