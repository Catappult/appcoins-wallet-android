package com.asfoundation.wallet.ui.backup

import android.os.Build
import android.os.Bundle
import androidx.documentfile.provider.DocumentFile
import com.asfoundation.wallet.backup.FileInteract
import com.asfoundation.wallet.interact.ExportWalletInteract
import com.asfoundation.wallet.logging.Logger
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.io.File

class BackupCreationPresenter(
    private val activityView: BackupActivityView,
    private val view: BackupCreationView,
    private val exportWalletInteract: ExportWalletInteract,
    private val fileInteract: FileInteract,
    private val logger: Logger,
    private val networkScheduler: Scheduler,
    private val viewScheduler: Scheduler,
    private val disposables: CompositeDisposable,
    private val walletAddress: String,
    private val password: String,
    private val temporaryPath: File?,
    private val downloadsPath: File?) {

  companion object {
    private const val FILE_SHARED_KEY = "FILE_SHARED"
    private const val KEYSTORE_KEY = "KEYSTORE"
    private const val FILE_NAME_KEY = "FILE_NAME"
  }

  private var fileShared = false
  private var cachedKeystore = ""
  private var cachedFileName: String? = null

  fun present(savedInstance: Bundle?) {
    savedInstance?.let {
      fileShared = it.getBoolean(FILE_SHARED_KEY)
      cachedKeystore = it.getString(KEYSTORE_KEY, "")
      cachedFileName = it.getString(FILE_NAME_KEY)
    }
    createBackUpFile()
    handlePositiveButtonClick()
    handleSaveAgainClick()
    handlePermissionGiven()
    handleDialogCancelClick()
    handleDialogSaveClick()
    handleSystemFileIntentResult()
  }

  private fun handleSystemFileIntentResult() {
    disposables.add(activityView.onSystemFileIntentResult()
        .observeOn(networkScheduler)
        .flatMapCompletable {
          if (it.documentFile != null && cachedFileName != null) {
            createAndSaveFile(it.documentFile, cachedFileName!!)
          } else {
            Completable.fromAction { view.closeDialog() }
          }
        }
        .subscribe({}, { showError(it) }))
  }

  private fun handleDialogSaveClick() {
    disposables.add(view.getDialogSaveClick()
        .doOnNext {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cachedFileName = it
            activityView.openSystemFileDirectory(it)
          } else {
            handleDialogSaveClickBelowAndroidQ(it)
          }
        }
        .subscribe())
  }

  private fun handleDialogSaveClickBelowAndroidQ(fileName: String) {
    disposables.add(fileInteract.createAndSaveFile(cachedKeystore, downloadsPath, fileName)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnComplete {
          view.closeDialog()
          activityView.showSuccessScreen()
        }
        .doOnError { showError(it) }
        .subscribe({}, { showError(it) }))
  }

  private fun handleDialogCancelClick() {
    disposables.add(view.getDialogCancelClick()
        .doOnNext { view.closeDialog() }
        .subscribe())
  }

  private fun createBackUpFile() {
    disposables.add(exportWalletInteract.export(walletAddress, password)
        .doOnSuccess { cachedKeystore = it }
        .flatMapCompletable { fileInteract.createTmpFile(walletAddress, it, temporaryPath) }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnComplete { view.enableSaveButton() }
        .doOnError { showError(it) }
        .subscribe({}, { showError(it) }))
  }

  private fun handlePositiveButtonClick() {
    disposables.add(view.getPositiveButtonClick()
        .observeOn(viewScheduler)
        .doOnNext {
          if (fileShared) {
            activityView.closeScreen()
          } else {
            val file = fileInteract.getCachedFile()
            if (file == null) showError("Error retrieving file")
            else {
              view.shareFile(fileInteract.getUriFromFile(file))
              fileShared = true
            }
          }
        }
        .subscribe())
  }

  private fun handleSaveAgainClick() {
    disposables.add(view.getSaveAgainClick()
        .doOnNext { activityView.startWalletBackup() }
        .subscribe())
  }

  private fun handlePermissionGiven() {
    disposables.add(activityView.onPermissionGiven()
        .doOnNext {
          downloadsPath?.let {
            view.showSaveOnDeviceDialog(fileInteract.getDefaultBackupFileFullName(walletAddress),
                it.path)
          } ?: showError("Unable to retrieve path")
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun createAndSaveFile(documentFile: DocumentFile,
                                fileName: String): Completable {
    return fileInteract.createAndSaveFile(cachedKeystore, documentFile, fileName)
        .observeOn(viewScheduler)
        .doOnComplete {
          fileInteract.saveChosenUri(documentFile.uri)
          view.closeDialog()
          activityView.showSuccessScreen()
        }
        .doOnError { showError(it) }
  }

  fun onResume() {
    if (fileShared) {
      view.showConfirmation()
      fileInteract.deleteFile()
    }
  }

  fun stop() {
    fileInteract.deleteFile()
    disposables.clear()
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(FILE_SHARED_KEY, fileShared)
    outState.putString(KEYSTORE_KEY, cachedKeystore)
    outState.putString(FILE_NAME_KEY, cachedFileName)
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    logger.log("BackupCreationPresenter", throwable)
    view.showError()
  }

  private fun showError(message: String) {
    logger.log("BackupCreationPresenter", message)
    view.showError()
  }
}
