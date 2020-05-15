package com.asfoundation.wallet.interact

import android.net.Uri
import android.os.Build
import com.asfoundation.wallet.backup.FileInteractor
import com.asfoundation.wallet.interact.rx.operator.Operators
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.util.ImportError
import com.asfoundation.wallet.util.ImportErrorType
import io.reactivex.Completable
import io.reactivex.Single

class ImportWalletInteractor(private val walletRepository: WalletRepositoryType,
                             private val setDefaultWalletInteract: SetDefaultWalletInteract,
                             private val passwordStore: PasswordStore,
                             private val preferencesRepositoryType: PreferencesRepositoryType,
                             private val fileInteractor: FileInteractor) {

  fun isKeystore(key: String): Boolean {
    return key.contains("{")
  }

  fun importKeystore(keystore: String, password: String = ""): Single<WalletModel> {
    return passwordStore.generatePassword()
        .flatMap { newPassword ->
          walletRepository.importKeystoreToWallet(keystore, password, newPassword)
              .compose(Operators.savePassword(passwordStore, walletRepository, newPassword))
        }
        .doOnSuccess { preferencesRepositoryType.setWalletImportBackup(it.address) }
        .map { WalletModel(it.address) }
        .onErrorReturn { mapError(keystore, it) }
  }

  fun importPrivateKey(privateKey: String?): Single<WalletModel> {
    return passwordStore.generatePassword()
        .flatMap { newPassword ->
          walletRepository.importPrivateKeyToWallet(privateKey, newPassword)
              .compose(Operators.savePassword(passwordStore, walletRepository, newPassword))
        }
        .map { WalletModel(it.address) }
        .doOnSuccess { preferencesRepositoryType.setWalletImportBackup(it.address) }
        .onErrorReturn { WalletModel(ImportError(ImportErrorType.GENERIC)) }
  }

  fun setDefaultWallet(address: String): Completable {
    return setDefaultWalletInteract.set(address)
  }

  fun getPath(): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      preferencesRepositoryType.getChosenUri()
          ?.let { Uri.parse(it) }
    } else {
      fileInteractor.getDownloadPath()
          ?.let { fileInteractor.getUriFromFile(it) }
    }
  }

  private fun mapError(keystore: String, throwable: Throwable): WalletModel {
    if (throwable.message != null) {
      if ((throwable.message as String).contains("Invalid Keystore", true)) {
        return WalletModel(ImportError(ImportErrorType.INVALID_KEYSTORE))
      }
      return when (throwable.message) {
        "Invalid password provided" -> WalletModel(keystore,
            ImportError(ImportErrorType.INVALID_PASS))
        "Already added" -> WalletModel(ImportError(ImportErrorType.ALREADY_ADDED))
        else -> WalletModel(ImportError(ImportErrorType.GENERIC))
      }
    } else {
      return WalletModel(ImportError(ImportErrorType.GENERIC))
    }
  }

  fun readFile(fileUri: Uri?): Single<String> {
    return fileInteractor.readFile(fileUri)
  }
}