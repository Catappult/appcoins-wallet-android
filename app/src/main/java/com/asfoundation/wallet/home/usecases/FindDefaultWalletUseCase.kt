package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.repository.WalletRepositoryType
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FindDefaultWalletUseCase @Inject constructor(
    private val walletRepository: com.appcoins.wallet.feature.walletInfo.data.repository.WalletRepositoryType) {

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