package com.asfoundation.wallet.backup.use_cases

import com.asfoundation.wallet.wallets.domain.WalletInfo
import io.reactivex.Completable
import io.reactivex.Single
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import javax.inject.Inject

class UpdateWalletPurchasesCountUseCase
@Inject
constructor(private val commonsPreferencesDataSource: CommonsPreferencesDataSource) {

  operator fun invoke(walletInfo: WalletInfo): Completable {
    return if (walletInfo.hasBackup.not()) {
      Single.just(commonsPreferencesDataSource.getWalletPurchasesCount(walletInfo.wallet))
        .map { it + 1 }
        .flatMapCompletable {
          Completable.fromAction {
            commonsPreferencesDataSource.incrementWalletPurchasesCount(walletInfo.wallet, it)
          }
        }
    } else {
      Completable.complete()
    }
  }
}
