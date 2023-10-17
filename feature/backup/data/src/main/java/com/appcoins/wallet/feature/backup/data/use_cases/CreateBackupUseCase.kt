package com.appcoins.wallet.feature.backup.data.use_cases

import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class CreateBackupUseCase @Inject constructor(private val walletRepository: WalletRepositoryType,
                                              private val passwordStore: PasswordStore) {

  operator fun invoke(walletAddress: String, password: String): Single<String> {
    return Single.fromCallable {
      val retrievedPassword = passwordStore.getPassword(walletAddress).blockingGet()
      walletRepository.exportWallet(walletAddress, retrievedPassword, password).blockingGet()
    }
  }
}