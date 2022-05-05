package com.asfoundation.wallet.backup.use_cases

import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class UpdateWalletPurchasesCountUseCase @Inject constructor(
  private val getWalletInfoUseCase: GetWalletInfoUseCase,
  private val sharedPreferencesRepository: SharedPreferencesRepository
) {

  operator fun invoke(walletAddress: String): Completable {
    return getWalletInfoUseCase(null, cached = false, updateFiat = false)
      .flatMapCompletable { walletInfo ->
        if (walletInfo.hasBackup.not()) {
          Single.just(sharedPreferencesRepository.getWalletPurchasesCount(walletAddress))
            .map { it + 1 }
            .flatMapCompletable {
              sharedPreferencesRepository.incrementWalletPurchasesCount(walletAddress, it)
            }
        } else {
          Completable.complete()
        }
      }
  }
}