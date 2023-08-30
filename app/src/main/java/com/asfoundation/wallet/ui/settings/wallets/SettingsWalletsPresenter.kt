package com.asfoundation.wallet.ui.settings.wallets

import io.reactivex.disposables.CompositeDisposable

class SettingsWalletsPresenter(
  private val view: SettingsWalletsView,
  private val navigator: SettingsWalletsNavigator,
  private val disposables: CompositeDisposable
) {

  fun present() {
    handleOutsideOfBottomSheetClick()
  }

  private fun handleOutsideOfBottomSheetClick() {
    disposables.add(view.outsideOfBottomSheetClick()
      .doOnNext { navigator.hideBottomSheet() }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()
}
