package com.asfoundation.wallet.ui.settings.wallets.bottomsheet

import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.ui.wallets.WalletsModel
import io.reactivex.disposables.CompositeDisposable

class SettingsWalletsBottomSheetPresenter(
    private val view: SettingsWalletsBottomSheetView,
    private val disposables: CompositeDisposable,
    private val walletsEventSender: WalletsEventSender,
    private val walletsModel: WalletsModel) {

  fun present() {
    view.setupUi(walletsModel.walletsBalance)
    handleWalletCardClick()
  }

  private fun handleWalletCardClick() {
    disposables.add(view.walletCardClicked()
        .doOnNext {
          walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
              WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_SUCCESS)
        }
        .doOnNext { view.navigateToBackup(it) }
        .doOnError {
          walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
              WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_FAIL)
        }
        .subscribe({}, { it.printStackTrace() }))
  }


  fun stop() = disposables.clear()
}
