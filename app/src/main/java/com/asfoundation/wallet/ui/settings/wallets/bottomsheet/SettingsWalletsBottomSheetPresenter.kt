package com.asfoundation.wallet.ui.settings.wallets.bottomsheet

import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.asfoundation.wallet.backup.entryBottomSheet.BackupEntryChooseWalletBottomSheetData
import com.asfoundation.wallet.backup.entryBottomSheet.BackupEntryChooseWalletBottomSheetNavigator
import com.asfoundation.wallet.backup.entryBottomSheet.BackupEntryChooseWalletBottomSheetView
import io.reactivex.disposables.CompositeDisposable

class SettingsWalletsBottomSheetPresenter(
  private val view: BackupEntryChooseWalletBottomSheetView,
  private val navigator: BackupEntryChooseWalletBottomSheetNavigator,
  private val disposables: CompositeDisposable,
  private val walletsEventSender: WalletsEventSender,
  private val data: BackupEntryChooseWalletBottomSheetData
) {

  fun present() {
    /*
    view.setupUi(data.walletsModel.wallets)
    handleWalletCardClick()

     */
  }

  private fun handleWalletCardClick() {
    /*
    disposables.add(view.walletCardClicked()
        .doOnNext { walletAddress ->
          walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
              WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_SUCCESS)

          navigator.navigateToBackup(walletAddress)
        }
        .doOnError {
          walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
              WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_FAIL)
        }
        .subscribe({}, { it.printStackTrace() }))

     */
  }


  fun stop() = disposables.clear()
}
