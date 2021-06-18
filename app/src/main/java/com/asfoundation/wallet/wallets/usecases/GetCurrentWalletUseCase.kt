package com.asfoundation.wallet.wallets.usecases

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.WalletRepositoryType
import io.reactivex.Single

class GetCurrentWalletUseCase(private val walletRepository: WalletRepositoryType) {

  operator fun invoke(): Single<Wallet> {
    return walletRepository.getDefaultWallet()
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