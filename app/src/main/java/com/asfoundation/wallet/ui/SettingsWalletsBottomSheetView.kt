package com.asfoundation.wallet.ui

import com.asfoundation.wallet.ui.wallets.WalletBalance
import io.reactivex.Observable

interface SettingsWalletsBottomSheetView {

  fun setupUi(walletsBalance: List<WalletBalance>)

  fun walletCardClicked(): Observable<String>

  fun navigateToBackup(address: String)
}

