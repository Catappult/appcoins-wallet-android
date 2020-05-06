package com.asfoundation.wallet.ui.backup

import io.reactivex.disposables.CompositeDisposable

class BackupCreationPresenter(private val activityView: BackupActivityView,
                              private val view: BackupCreationView) {

  private val disposables = CompositeDisposable()
  private var fileShared = false

  fun presenter() {
    handleBackupClick()
    handleCloseBtnClick()
  }

  private fun handleBackupClick() {
    disposables.add(view.getPositiveButtonClick()
        .doOnNext {
          if (fileShared) {
            activityView.closeScreen()
          } else {
            view.shareFile("")
            fileShared = true
          }
        }
        .subscribe())
  }

  private fun handleCloseBtnClick() {
    disposables.add(view.getNegativeButtonClick()
        .doOnNext {
          activityView.showSuccessScreen()
        }
        .subscribe())
  }

  fun onResume() {
    if (fileShared) {
      view.showConfirmation()
    }
  }
}
