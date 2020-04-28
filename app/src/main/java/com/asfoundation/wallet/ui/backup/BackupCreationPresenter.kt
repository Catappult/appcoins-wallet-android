package com.asfoundation.wallet.ui.backup

class BackupCreationPresenter(private val view: BackupCreationView) {

  private var fileShared = false
  fun presenter() {
    handleBackupClick()
  }

  private fun handleBackupClick() {
    view.getPositiveButtonClick()
        .map {
          view.shareFile("")
          fileShared = true
        }
        .subscribe()
  }

  fun onResume() {
    if (fileShared) {
      view.showConfirmation()
    }
  }


}
