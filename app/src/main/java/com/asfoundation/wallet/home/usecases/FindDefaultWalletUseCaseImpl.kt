package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.FindDefaultWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetActiveWalletUseCase
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(FindDefaultWalletUseCase::class)
class FindDefaultWalletUseCaseImpl @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val setActiveWalletUseCase: SetActiveWalletUseCase
): FindDefaultWalletUseCase {

  override operator fun invoke(): Single<Wallet> {
    return walletRepository.getDefaultWallet()
      .subscribeOn(Schedulers.io())
      .onErrorResumeNext {
        walletRepository.fetchWallets()
          .filter { it.isNotEmpty() }
          .map { it.first() }
          .flatMapCompletable { setActiveWalletUseCase(it.address) }
          .andThen(walletRepository.getDefaultWallet())
      }
  }
}