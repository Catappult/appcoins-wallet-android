package com.asfoundation.wallet.ui.backup.use_cases

import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class CreateBackupUseCase @Inject constructor(private val walletRepository: WalletRepositoryType,
                                              private val passwordStore: PasswordStore) {

  operator fun invoke(walletAddress: String,
                      password: String): Single<String> {
    return passwordStore.getPassword(walletAddress)
        .flatMap { walletRepository.exportWallet(walletAddress, it, password) }
  }
}