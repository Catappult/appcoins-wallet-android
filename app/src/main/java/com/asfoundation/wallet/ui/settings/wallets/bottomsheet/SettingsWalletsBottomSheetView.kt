package com.asfoundation.wallet.ui.settings.wallets.bottomsheet

import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import io.reactivex.Observable

interface SettingsWalletsBottomSheetView {

  fun setupUi(walletsBalance: List<WalletInfoSimple>)

  fun walletCardClicked(): Observable<String>
}

