package com.asfoundation.wallet.ui.settings.wallets

import com.asfoundation.wallet.ui.settings.SettingsActivityView
import io.reactivex.disposables.CompositeDisposable

class SettingsWalletsPresenter(private val view: SettingsWalletsView,
                               private val activityView: SettingsActivityView,
                               private val disposables: CompositeDisposable) {

  fun present() {
    handleOutsideOfBottomSheetClick()
  }

  private fun handleOutsideOfBottomSheetClick() {
    disposables.add(view.outsideOfBottomSheetClick()
        .doOnNext { activityView.hideBottomSheet() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()
}
