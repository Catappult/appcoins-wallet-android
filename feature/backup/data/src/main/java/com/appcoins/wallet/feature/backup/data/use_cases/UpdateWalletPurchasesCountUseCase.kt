package com.appcoins.wallet.feature.backup.data.use_cases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import io.reactivex.Completable
import io.reactivex.Single
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
