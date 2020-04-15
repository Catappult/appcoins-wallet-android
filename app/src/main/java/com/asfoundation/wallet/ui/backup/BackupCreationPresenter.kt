package com.asfoundation.wallet.ui.backup

class BackupCreationPresenter(private val view: BackupCreationView) {

  fun presenter() {
    handleBackupClick()
  }

  private fun handleBackupClick() {
    view.getBackupClick()
        .map { view.shareFile("") }.subscribe()

  }


}
