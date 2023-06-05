package com.asfoundation.wallet.recover.use_cases

import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.recover.result.RecoverPasswordResult
import com.asfoundation.wallet.recover.result.RecoverPasswordResultMapper
import io.reactivex.Single
import javax.inject.Inject

class RecoverPasswordKeystoreUseCase @Inject constructor(
        private val walletRepository: WalletRepositoryType,
        private val passwordStore: PasswordStore,
) {

  operator fun invoke(
    keyStore: WalletKeyStore,
    password: String = ""
  ): Single<RecoverPasswordResult> {
    return passwordStore.generatePassword()
      .flatMap { newPassword ->
        walletRepository.restoreKeystoreToWallet(keyStore.contents, password, newPassword)
      }
      .flatMap {
        RecoverPasswordResultMapper(keyStore).map(it)
      }
  }
}