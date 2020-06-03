package com.asfoundation.wallet.ui

import com.asfoundation.wallet.ui.wallets.WalletsModel
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class SettingsWalletsBottomSheetPresenter(
    private val view: SettingsWalletsBottomSheetView,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler,
    private val walletsModel: WalletsModel) {

  fun present() {
    view.setupUi(walletsModel.walletsBalance)
    handleWalletCardClick()
  }

  private fun handleWalletCardClick() {
    disposables.add(view.walletCardClicked()
        .doOnNext { view.navigateToBackup(it) }
        .subscribe({}, { it.printStackTrace() }))
  }


  fun stop() = disposables.clear()
}
