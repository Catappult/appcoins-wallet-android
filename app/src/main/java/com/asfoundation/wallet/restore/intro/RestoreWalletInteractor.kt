package com.asfoundation.wallet.restore.intro

import android.net.Uri
import android.os.Build
import com.asfoundation.wallet.backup.FileInteractor
import com.asfoundation.wallet.interact.SetDefaultWalletInteractor
import com.asfoundation.wallet.interact.rx.operator.Operators
import com.asfoundation.wallet.repository.BackupRestorePreferencesRepository
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.util.RestoreError
import com.asfoundation.wallet.util.RestoreErrorType
import com.asfoundation.wallet.wallets.WalletModel
import io.reactivex.Completable
import io.reactivex.Single

class RestoreWalletInteractor(private val walletRepository: WalletRepositoryType,
                              private val setDefaultWalletInteractor: SetDefaultWalletInteractor,
                              private val balanceInteractor: BalanceInteractor,
                              private val passwordStore: PasswordStore,
                              private val backupRestorePreferencesRepository: BackupRestorePreferencesRepository,
                              private val fileInteractor: FileInteractor) {

  fun isKeystore(key: String): Boolean = key.contains("{")

  fun getOverallBalance(address: String) = balanceInteractor.getTotalBalance(address)

  fun restoreKeystore(keystore: String, password: String = ""): Single<WalletModel> {
    return passwordStore.generatePassword()
        .flatMap { newPassword ->
          walletRepository.restoreKeystoreToWallet(keystore, password, newPassword)
              .compose(Operators.savePassword(passwordStore, walletRepository, newPassword))
        }
        .doOnSuccess { backupRestorePreferencesRepository.setWalletRestoreBackup(it.address) }
        .map { WalletModel(it.address) }
        .onErrorReturn { mapError(keystore, it) }
  }

  fun restorePrivateKey(privateKey: String?): Single<WalletModel> {
    return passwordStore.generatePassword()
        .flatMap { newPassword ->
          walletRepository.restorePrivateKeyToWallet(privateKey, newPassword)
              .compose(Operators.savePassword(passwordStore, walletRepository, newPassword))
        }
        .map { WalletModel(it.address) }
        .doOnSuccess { backupRestorePreferencesRepository.setWalletRestoreBackup(it.address) }
        .onErrorReturn { WalletModel(RestoreError(RestoreErrorType.GENERIC)) }
  }

  fun setDefaultWallet(address: String): Completable = setDefaultWalletInteractor.set(address)

  fun getPath(): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      backupRestorePreferencesRepository.getChosenUri()
          ?.let { Uri.parse(it) }
    } else {
      fileInteractor.getDownloadPath()
          ?.let { fileInteractor.getUriFromFile(it) }
    }
  }

  private fun mapError(keystore: String, throwable: Throwable): WalletModel {
    if (throwable.message != null) {
      if ((throwable.message as String).contains("Invalid Keystore", true)) {
        return WalletModel(
            RestoreError(RestoreErrorType.INVALID_KEYSTORE))
      }
      return when (throwable.message) {
        "Invalid password provided" -> WalletModel(
            keystore,
            RestoreError(RestoreErrorType.INVALID_PASS))
        "Already added" -> WalletModel(
            RestoreError(RestoreErrorType.ALREADY_ADDED))
        else -> WalletModel(
            RestoreError(RestoreErrorType.GENERIC))
      }
    } else {
      return WalletModel(RestoreError(RestoreErrorType.GENERIC))
    }
  }

  fun readFile(fileUri: Uri?): Single<String> = fileInteractor.readFile(fileUri)
}