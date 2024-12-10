package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class GetCurrentWalletUseCase @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val setActiveWalletUseCase: SetActiveWalletUseCase,
) {

  operator fun invoke(): Single<Wallet> {
    return walletRepository.getDefaultWallet()
      .onErrorResumeNext {
        walletRepository.fetchWallets()
          .filter { it.isNotEmpty() }
          .map { it.first() }
          .flatMapCompletable { setActiveWalletUseCase(it.address) }
          .andThen(walletRepository.getDefaultWallet())
      }
  }
}