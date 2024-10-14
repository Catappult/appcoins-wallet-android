package com.asfoundation.wallet.ui.settings.wallets

import com.asfoundation.wallet.backup.entryBottomSheet.BackupEntryChooseWalletView
import io.reactivex.disposables.CompositeDisposable

class SettingsWalletsPresenter(
  private val view: BackupEntryChooseWalletView,
  private val navigator: SettingsWalletsNavigator,
  private val disposables: CompositeDisposable
) {

  fun present() {
    handleOutsideOfBottomSheetClick()
  }

  private fun handleOutsideOfBottomSheetClick() {
    disposables.add(
      view
        .outsideOfBottomSheetClick()
        .doOnNext { navigator.hideBottomSheet() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()
}
