package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable

interface WalletsView {

  fun setupUi(totalWallets: Int, totalBalance: FiatValue,
              walletsBalanceList: List<WalletBalance>)

  fun otherWalletCardClicked(): Observable<String>
  fun activeWalletCardClicked(): Observable<String>
  fun navigateToWalletDetailView(walletAddress: String, isActive: Boolean)
  fun createNewWalletClicked(): Observable<Any>
  fun showCreatingAnimation()
  fun showWalletCreatedAnimation()
  fun restoreWalletClicked(): Observable<Any>
  fun navigateToRestoreView()
  fun onBottomSheetHeaderClicked(): Observable<Any>
  fun changeBottomSheetState()
}
