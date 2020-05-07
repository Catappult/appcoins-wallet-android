package com.asfoundation.wallet.ui.backup

import android.net.Uri
import com.asfoundation.wallet.backup.FileInteract
import com.asfoundation.wallet.interact.ExportWalletInteract
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.io.File

class BackupCreationPresenter(
    private val activityView: BackupActivityView,
    private val view: BackupCreationView,
    private val exportWalletInteract: ExportWalletInteract,
    private val fileInteract: FileInteract,
    private val networkScheduler: Scheduler,
    private val viewScheduler: Scheduler) {

  private val disposables = CompositeDisposable()
  private var fileShared = false

  fun presenter(walletAddress: String, password: String, path: File) {
    createBackUpFile(walletAddress, password, path)
    handleBackupClick()
    handleCloseBtnClick()
  }

  private fun createBackUpFile(walletAddress: String, password: String,
                               path: File) {
    disposables.add(exportWalletInteract.export(walletAddress, password)
        .flatMap {
          fileInteract.createTmpFile(walletAddress, it, path)
        }
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { success -> if (success) view.enableSaveButton() else view.showError() }
        .doOnError {
          it.printStackTrace()
          view.showError()
        }
        .subscribe({}, {
          it.printStackTrace()
          view.showError()
        }))
  }

  private fun handleBackupClick() {
    disposables.add(view.getPositiveButtonClick()
        .doOnNext {
          if (fileShared) {
            fileInteract.deleteFile()
            activityView.closeScreen()
          } else {
            val file = fileInteract.getCachedFile()
            if (file == null) view.showError()
            else {
              view.shareFile(Uri.fromFile(file))
              fileShared = true
            }
          }
        }
        .subscribe())
  }

  private fun handleCloseBtnClick() {
    disposables.add(view.getNegativeButtonClick()
        .doOnNext { activityView.showSuccessScreen() }
        .subscribe())
  }

  fun onResume() {
    if (fileShared) view.showConfirmation()
  }

  fun stop() {
    fileInteract.deleteFile()
    disposables.clear()
  }
}
