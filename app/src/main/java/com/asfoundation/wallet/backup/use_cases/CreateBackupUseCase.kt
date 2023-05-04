package com.asfoundation.wallet.backup.use_cases

import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

class CreateBackupUseCase @Inject constructor(private val walletRepository: WalletRepositoryType,
                                              private val passwordStore: PasswordStore) {
  suspend operator fun invoke(
      walletAddress: String,
      password: String): String {
    val retrievedPassword = passwordStore.getPassword(walletAddress).await()
    return walletRepository.exportWallet(walletAddress, retrievedPassword, password).await()
  }
}