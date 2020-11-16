package com.asfoundation.wallet.ui.backup.creation

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.asfoundation.wallet.backup.FileInteractor
import com.asfoundation.wallet.interact.ExportWalletInteractor
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File

class BackupCreationInteractor(private val exportWalletInteractor: ExportWalletInteractor,
                               private val fileInteractor: FileInteractor) {

  fun createAndSaveFile(cachedKeystore: String, downloadsPath: File?,
                        fileName: String): Completable {
    return fileInteractor.createAndSaveFile(cachedKeystore, downloadsPath, fileName)
  }

  fun createAndSaveFile(cachedKeystore: String, downloadsPath: DocumentFile,
                        fileName: String): Completable {
    return fileInteractor.createAndSaveFile(cachedKeystore, downloadsPath, fileName)
  }

  fun export(walletAddress: String, password: String): Single<String> {
    return exportWalletInteractor.export(walletAddress, password)
  }

  fun createTmpFile(walletAddress: String, keystore: String, temporaryPath: File?): Completable {
    return fileInteractor.createTmpFile(walletAddress, keystore, temporaryPath)
  }

  fun getCachedFile(): File? = fileInteractor.getCachedFile()

  fun getUriFromFile(file: File): Uri = fileInteractor.getUriFromFile(file)

  fun getDefaultBackupFileName(walletAddress: String): String {
    return fileInteractor.getDefaultBackupFileName(walletAddress)
  }

  fun saveChosenUri(uri: Uri) = fileInteractor.saveChosenUri(uri)

  fun deleteFile() = fileInteractor.deleteFile()
}
