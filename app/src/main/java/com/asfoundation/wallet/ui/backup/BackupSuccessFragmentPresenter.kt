package com.asfoundation.wallet.ui.backup

import io.reactivex.disposables.CompositeDisposable

class BackupSuccessFragmentPresenter(private val view: BackupSuccessFragmentView,
                                     private val activityView: BackupActivityView,
                                     private val disposables: CompositeDisposable) {

  fun present() {
    handleCloseBtnClick()
  }

  private fun handleCloseBtnClick() {
    disposables.add(view.getCloseButtonClick()
        .doOnNext { activityView.closeScreen() }
        .subscribe())
  }

  fun stop() = disposables.clear()
}
