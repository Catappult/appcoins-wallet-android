package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

class ExportWalletInteract(private val walletRepository: WalletRepositoryType,
                           private val passwordStore: PasswordStore) {

  fun export(wallet: Wallet, backupPassword: String?): Single<String> {
    return passwordStore.getPassword(wallet.address)
        .flatMap { walletRepository.exportWallet(wallet, it, backupPassword) }
        .observeOn(AndroidSchedulers.mainThread())
  }

}