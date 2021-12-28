package com.asfoundation.wallet.recover.use_cases

import android.util.Log
import com.asfoundation.wallet.interact.rx.operator.Operators
import com.asfoundation.wallet.recover.RecoverWalletResult
import com.asfoundation.wallet.recover.SuccessfulRecover
import com.asfoundation.wallet.repository.BackupRestorePreferencesRepository
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.ObserveWalletInfoUseCase
import io.reactivex.Single

class RecoverKeystoreUseCase(private val walletRepository: WalletRepositoryType,
                             private val passwordStore: PasswordStore,
                             private val backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
                             private val recoverErrorMapperUseCase: RecoverErrorMapperUseCase,
                             private val observeWalletInfoUseCase: ObserveWalletInfoUseCase,
                             private val currencyFormatUtils: CurrencyFormatUtils) {

  operator fun invoke(keystore: String, password: String = ""): Single<RecoverWalletResult> {
    return passwordStore.generatePassword()
        .flatMap { newPassword ->
          walletRepository.restoreKeystoreToWallet(keystore, password, newPassword)
        }
        .doOnSuccess {
          when (it) {
            is SuccessfulRecover -> backupRestorePreferencesRepository.setWalletRestoreBackup(
                it.address!!)
            else -> Unit
          }
        }

  }
}