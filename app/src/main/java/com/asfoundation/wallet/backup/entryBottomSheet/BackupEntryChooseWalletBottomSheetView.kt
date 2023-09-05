package com.asfoundation.wallet.backup.entryBottomSheet

import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import io.reactivex.Observable

interface BackupEntryChooseWalletBottomSheetView {

  fun setupUi(walletsBalance: List<WalletInfoSimple>)

  fun walletCardClicked(): Observable<String>

}

