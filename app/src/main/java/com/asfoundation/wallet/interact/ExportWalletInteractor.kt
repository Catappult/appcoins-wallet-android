package com.asfoundation.wallet.interact

import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

class ExportWalletInteractor(private val walletRepository: WalletRepositoryType,
                             private val passwordStore: PasswordStore) {

  fun export(walletAddress: String, backupPassword: String?): Single<String> {
    return passwordStore.getPassword(walletAddress)
        .flatMap { walletRepository.exportWallet(walletAddress, it, backupPassword) }
        .observeOn(AndroidSchedulers.mainThread())
  }

}