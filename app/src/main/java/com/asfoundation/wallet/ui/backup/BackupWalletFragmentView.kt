package com.asfoundation.wallet.ui.backup

import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable

interface BackupWalletFragmentView {
  fun showBalance(value: FiatValue)
  fun getBackupClick(): Observable<String>
  fun hideKeyboard()
}
