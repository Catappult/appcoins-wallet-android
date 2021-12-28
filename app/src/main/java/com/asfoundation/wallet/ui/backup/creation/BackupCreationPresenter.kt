package com.asfoundation.wallet.ui.backup.creation

import android.os.Build
import android.os.Bundle
import androidx.documentfile.provider.DocumentFile
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.appcoins.wallet.commons.Logger
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.io.File

class BackupCreationPresenter(private val view: BackupCreationView,
                              private val interactor: BackupCreationInteractor,
                              private val walletsEventSender: WalletsEventSender,
                              private val logger: Logger,
                              private val networkScheduler: Scheduler,
                              private val viewScheduler: Scheduler,
                              private val disposables: CompositeDisposable,
                              private val data: BackupCreationData,
                              private val navigator: BackupCreationNavigator,
                              private val temporaryPath: File?,
                              private val downloadsPath: File?) {

  companion object {
    private const val FILE_SHARED_KEY = "FILE_SHARED"
    private const val KEYSTORE_KEY = "KEYSTORE"
    private const val FILE_NAME_KEY = "FILE_NAME"
    private val TAG = BackupCreationPresenter::class.java.name
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
    handleFinishClick()
    handleFirstSaveClick()
    handleSaveAgainClick()
    handlePermissionGiven()
    handleDialogCancelClick()
    handleDialogSaveClick()
    handleSystemFileIntentResult()
  }

  private fun handleSystemFileIntentResult() {
    disposables.add(view.onSystemFileIntentResult()
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
            navigator.openSystemFileDirectory(it)
          } else {
            handleDialogSaveClickBelowAndroidQ(it)
          }
          walletsEventSender.sendWalletSaveFileEvent(WalletsAnalytics.ACTION_SAVE,
              WalletsAnalytics.STATUS_SUCCESS)
        }
        .doOnError {
          walletsEventSender.sendWalletSaveFileEvent(WalletsAnalytics.ACTION_SAVE,
              WalletsAnalytics.STATUS_FAIL, it.message)
        }
        .subscribe({}, { showError(it) }))
  }

  private fun handleDialogSaveClickBelowAndroidQ(fileName: String) {
    disposables.add(interactor.createAndSaveFile(cachedKeystore, downloadsPath, fileName)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnComplete {
          view.closeDialog()
          navigator.showSuccessScreen()
        }
        .doOnError { showError(it) }
        .subscribe({}, { showError(it) }))
  }

  private fun handleDialogCancelClick() {
    disposables.add(view.getDialogCancelClick()
        .doOnNext { view.closeDialog() }
        .doOnNext {
          walletsEventSender.sendWalletSaveFileEvent(WalletsAnalytics.ACTION_CANCEL,
              WalletsAnalytics.STATUS_FAIL)
        }
        .doOnError { t ->
          walletsEventSender.sendWalletSaveFileEvent(WalletsAnalytics.ACTION_CANCEL,
              WalletsAnalytics.STATUS_FAIL, t.message)
        }
        .subscribe({}, { view.closeDialog() }))
  }

  private fun createBackUpFile() {
    disposables.add(interactor.export(data.walletAddress, data.password)
        .doOnSuccess { cachedKeystore = it }
        .flatMapCompletable { interactor.createTmpFile(data.walletAddress, it, temporaryPath) }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnComplete { view.enableSaveButton() }
        .doOnError { showError(it) }
        .subscribe({}, { showError(it) }))
  }

  private fun handleFinishClick() {
    disposables.add(view.getFinishClick()
        .observeOn(viewScheduler)
        .doOnNext {
          walletsEventSender.sendWalletConfirmationBackupEvent(WalletsAnalytics.ACTION_FINISH)
          view.closeScreen()
        }
        .subscribe({}, { view.closeScreen() })
    )
  }

  private fun handleFirstSaveClick() {
    disposables.add(view.getFirstSaveClick()
        .observeOn(viewScheduler)
        .doOnNext { shareFile(interactor.getCachedFile()) }
        .subscribe({}, { logger.log(TAG, it) }))
  }

  private fun shareFile(file: File?) {
    if (file == null) {
      showError("Error retrieving file")
    } else {
      fileShared = true
      view.shareFile(interactor.getUriFromFile(file))
      walletsEventSender.sendSaveBackupEvent(WalletsAnalytics.ACTION_SAVE)
      interactor.saveBackedUpOnce()
    }
  }

  private fun handleSaveAgainClick() {
    disposables.add(view.getSaveOnDeviceButton()
        .doOnNext { view.askForWritePermissions() }
        .doOnNext {
          walletsEventSender.sendWalletConfirmationBackupEvent(WalletsAnalytics.ACTION_SAVE)
          interactor.saveBackedUpOnce()
        }
        .subscribe({}, { logger.log(TAG, it) }))
  }

  private fun handlePermissionGiven() {
    disposables.add(view.onPermissionGiven()
        .doOnNext {
          navigator.showSaveOnDeviceScreen(data.walletAddress, data.password)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun createAndSaveFile(documentFile: DocumentFile,
                                fileName: String): Completable {
    return interactor.createAndSaveFile(cachedKeystore, documentFile, fileName)
        .observeOn(viewScheduler)
        .doOnComplete {
          interactor.saveChosenUri(documentFile.uri)
          view.closeDialog()
          navigator.showSuccessScreen()
        }
  }

  fun onResume() {
    if (fileShared) {
      view.showConfirmation()
      interactor.deleteFile()
    }
  }

  fun stop() {
    interactor.deleteFile()
    disposables.clear()
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(FILE_SHARED_KEY, fileShared)
    outState.putString(KEYSTORE_KEY, cachedKeystore)
    outState.putString(FILE_NAME_KEY, cachedFileName)
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    logger.log(TAG, throwable)
    view.showError()
  }

  private fun showError(message: String) {
    logger.log(TAG, message)
    view.showError()
  }
}
