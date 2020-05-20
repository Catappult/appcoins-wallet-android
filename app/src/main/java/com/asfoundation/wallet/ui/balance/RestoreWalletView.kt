package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.util.RestoreErrorType
import io.reactivex.Observable

interface RestoreWalletView {

  fun restoreFromStringClick(): Observable<String>

  fun restoreFromFileClick(): Observable<Any>

  fun navigateToPasswordView(keystore: String)

  fun showError(type: RestoreErrorType)
}
