package com.asfoundation.wallet.ui.backup.success

import io.reactivex.disposables.CompositeDisposable

class BackupSuccessPresenter(private val view: BackupSuccessFragmentView,
                             private val disposables: CompositeDisposable) {

  fun present() {
    handleCloseBtnClick()
  }

  private fun handleCloseBtnClick() {
    disposables.add(view.getCloseButtonClick()
        .doOnNext { view.closeScreen() }
        .subscribe())
  }

  fun stop() = disposables.clear()
}
