package com.appcoins.wallet.feature.walletInfo.data.wallet

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetActiveWalletUseCase
import io.reactivex.Single
import javax.inject.Inject

class FindDefaultWalletInteract @Inject constructor(
  private val walletRepository: WalletRepositoryType,
  private val rxSchedulers: RxSchedulers,
  private val setActiveWalletUseCase: SetActiveWalletUseCase
) {

  fun find(): Single<Wallet> {
    return walletRepository.getDefaultWallet()
      .subscribeOn(rxSchedulers.io)
      .onErrorResumeNext {
        walletRepository.fetchWallets()
          .filter { it.isNotEmpty() }
          .map { it.first() }
          .flatMapCompletable { setActiveWalletUseCase(it.address) }
          .andThen(walletRepository.getDefaultWallet())
      }
  }
}