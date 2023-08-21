package com.appcoins.wallet.feature.backup.data.use_cases

import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
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