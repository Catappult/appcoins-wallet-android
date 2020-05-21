package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import io.reactivex.Observable

interface WalletDetailsView {
  fun populateUi(balanceScreenModel: BalanceScreenModel)
  fun copyClick(): Observable<Any>
  fun shareClick(): Observable<Any>
  fun setAddressToClipBoard(walletAddress: String)
  fun showShare(walletAddress: String)
  fun makeWalletActiveClick(): Observable<Any>
  fun navigateToBalanceView()
  fun backupInactiveWalletClick(): Observable<Any>
  fun backupActiveWalletClick(): Observable<Any>
  fun removeWalletClick(): Observable<Any>
  fun navigateToBackupView(walletAddress: String)
  fun navigateToRemoveWalletView(walletAddress: String)
}
