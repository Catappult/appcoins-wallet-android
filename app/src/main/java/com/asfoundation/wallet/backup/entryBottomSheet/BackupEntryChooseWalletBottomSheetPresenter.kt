package com.asfoundation.wallet.backup.entryBottomSheet

import androidx.navigation.NavController
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.asfoundation.wallet.backup.entryBottomSheet.WalletList.walletsCustom
import io.reactivex.disposables.CompositeDisposable

class BackupEntryChooseWalletBottomSheetPresenter(
  private val view: BackupEntryChooseWalletBottomSheetView,
  private val navigator: BackupEntryChooseWalletBottomSheetNavigator,
  private val disposables: CompositeDisposable,
  private val walletsEventSender: WalletsEventSender,
  private val data: BackupEntryChooseWalletBottomSheetData,
) {

  var walletName2 : String? = ""


  fun present(data: WalletsModel, navController: NavController) {
    if (walletsCustom?.first { it.isActiveWallet }?.backupWalletActive != data.wallets.first { it.isActiveWallet }.backupWalletActive) {
      data.wallets.first { it.isActiveWallet }.backupWalletActive = true
      walletsCustom = data.wallets
      walletName2 = data.wallets.first().walletName
      view.setupUi(data.wallets)
      handleWalletCardClick(navController)
    } else {
      view.setupUi(walletsCustom!!)
      walletName2 = walletsCustom?.first()?.walletName
      handleWalletCardClick(navController)
    }
  }

  private fun handleWalletCardClick(navController: NavController) {
    disposables.add(view.walletCardClicked()
        .doOnNext { walletAddress ->
          walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
              WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_SUCCESS)

          walletsCustom?.first { it.backupWalletActive }?.backupWalletActive = false
          walletsCustom?.first { it.walletAddress == walletAddress }?.backupWalletActive = true
          walletName2 = walletsCustom?.first{ it.walletAddress == walletAddress }?.walletName
          navigator.navigateToBackup(walletAddress, walletName2!!, navController)

        }

        .doOnError {
          walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
              WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_FAIL)
        }
        .subscribe({}, { it.printStackTrace() }))
  }


  fun stop() = disposables.clear()
}
