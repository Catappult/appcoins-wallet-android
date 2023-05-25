package com.asfoundation.wallet.interact

import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class ExportWalletInteractor @Inject constructor(private val walletRepository: WalletRepositoryType,
                                                 private val passwordStore: PasswordStore) {

  fun export(walletAddress: String, backupPassword: String?): Single<String> {
    return passwordStore.getPassword(walletAddress)
        .flatMap { walletRepository.exportWallet(walletAddress, it, backupPassword) }
        .observeOn(AndroidSchedulers.mainThread())
  }

}