package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.util.ImportErrorType
import io.reactivex.Observable

interface ImportWalletView {

  fun importFromStringClick(): Observable<String>

  fun importFromFileClick(): Observable<Any>

  fun navigateToPasswordView(keystore: String)

  fun showError(type: ImportErrorType)
}
