package com.asfoundation.wallet.ui.settings.wallets.bottomsheet

import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import io.reactivex.disposables.CompositeDisposable

class SettingsWalletsBottomSheetPresenter(
  private val view: SettingsWalletsBottomSheetView,
  private val navigator: SettingsWalletsBottomSheetNavigator,
  private val disposables: CompositeDisposable,
  private val walletsEventSender: WalletsEventSender,
  private val data: SettingsWalletsBottomSheetData
) {

  fun present() {
    view.setupUi(data.walletsModel.wallets)
    handleWalletCardClick()
  }

  private fun handleWalletCardClick() {
    disposables.add(view.walletCardClicked()
      .doOnNext { walletAddress ->
        walletsEventSender.sendCreateBackupEvent(
          WalletsAnalytics.ACTION_CREATE,
          WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_SUCCESS
        )
        navigator.navigateToBackup(walletAddress)
      }
      .doOnError {
        walletsEventSender.sendCreateBackupEvent(
          WalletsAnalytics.ACTION_CREATE,
          WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_FAIL
        )
      }
      .subscribe({}, { it.printStackTrace() })
    )
  }


  fun stop() = disposables.clear()
}
