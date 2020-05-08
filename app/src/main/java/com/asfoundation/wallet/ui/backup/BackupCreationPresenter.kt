package com.asfoundation.wallet.ui.backup

import android.net.Uri
import android.os.Bundle
import com.asfoundation.wallet.backup.FileInteract
import com.asfoundation.wallet.interact.ExportWalletInteract
import com.asfoundation.wallet.logging.Logger
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
  }

  private var fileShared = false
  private var cachedKeystore = ""

  fun present(savedInstance: Bundle?) {
    savedInstance?.let {
      fileShared = it.getBoolean(FILE_SHARED_KEY)
      cachedKeystore = it.getString(KEYSTORE_KEY, "")
    }
    createBackUpFile()
    handlePositiveButtonClick()
    handleSaveAgainClick()
    handlePermissionGiven()
    handleDialogCancelClick()
    handleDialogSaveClick()
  }

  private fun handleDialogSaveClick() {
    disposables.add(view.getDialogSaveClick()
        .observeOn(networkScheduler)
        .flatMapSingle { fileInteract.createAndSaveFile(cachedKeystore, downloadsPath, it) }
        .observeOn(viewScheduler)
        .doOnNext { success ->
          if (success) {
            view.closeDialog()
            activityView.showSuccessScreen()
          } else showError("Unable to create tmp file")
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
        .flatMap {
          cachedKeystore = it
          fileInteract.createTmpFile(walletAddress, it, temporaryPath)
        }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { success ->
          if (success) view.enableSaveButton() else showError("Unable to create file")
        }
        .doOnError { showError(it) }
        .subscribe({}, { showError(it) }))
  }

  private fun handlePositiveButtonClick() {
    disposables.add(view.getPositiveButtonClick()
        .doOnNext {
          if (fileShared) {
            activityView.closeScreen()
          } else {
            val file = fileInteract.getCachedFile()
            if (file == null) showError("Error retrieving file")
            else {
              view.shareFile(Uri.fromFile(file))
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
