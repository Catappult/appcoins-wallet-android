package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetActiveWalletUseCase
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ObserveDefaultWalletUseCase @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val setActiveWalletUseCase: SetActiveWalletUseCase,
) {

  operator fun invoke(): Observable<Wallet> {
    return walletRepository.observeDefaultWallet()
      .subscribeOn(Schedulers.io())
      .onErrorResumeNext { _: Throwable ->
        return@onErrorResumeNext walletRepository.fetchWallets()
          .filter { it.isNotEmpty() }
          .map { it.first() }
          .flatMapCompletable { setActiveWalletUseCase(it.address) }
          .andThen(walletRepository.observeDefaultWallet())
      }
  }
}