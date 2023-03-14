package com.asfoundation.wallet.backup.use_cases

import repository.SharedPreferencesRepository
import com.asfoundation.wallet.wallets.domain.WalletInfo
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class UpdateWalletPurchasesCountUseCase @Inject constructor(
  private val sharedPreferencesRepository: SharedPreferencesRepository
) {

  operator fun invoke(walletInfo: WalletInfo): Completable {
    return if (walletInfo.hasBackup.not()) {
      Single.just(sharedPreferencesRepository.getWalletPurchasesCount(walletInfo.wallet))
        .map { it + 1 }
        .flatMapCompletable {
          sharedPreferencesRepository.incrementWalletPurchasesCount(walletInfo.wallet, it)
        }
    } else {
      Completable.complete()
    }
  }
}