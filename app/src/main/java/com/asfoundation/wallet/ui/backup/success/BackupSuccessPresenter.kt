package com.asfoundation.wallet.ui.backup.success
import io.reactivex.disposables.CompositeDisposable

class BackupSuccessPresenter(private val data: BackupSuccessData,
                             private val view: BackupSuccessFragmentView,
                             private val disposables: CompositeDisposable) {

  fun present() {
    handleCloseBtnClick()
    setSuccessInfo()
  }

  private fun setSuccessInfo() {
    var info = "Your backup file is stored in your device"
    if (data.email) {
      info = "Your backup file is in your email"
    }
    view.setSuccessInfo(info)
  }

  private fun handleCloseBtnClick() {
    disposables.add(view.getCloseButtonClick()
        .doOnNext { view.closeScreen() }
        .subscribe())
  }

  fun stop() = disposables.clear()
}
