package com.asfoundation.wallet.ui

import com.asfoundation.wallet.ui.wallets.WalletBalance
import io.reactivex.subjects.PublishSubject

interface SettingsWalletsBottomSheetView {

  fun setupUi(walletsBalance: List<WalletBalance>)
  fun walletCardClicked(): PublishSubject<String>
  fun navigateToBackup(address: String)
}

