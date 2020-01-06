package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.util.ImportErrorType
import io.reactivex.Observable

interface ImportWalletView {
  fun importFromStringClick(): Observable<String>
  fun navigateToPasswordView()
  fun showWalletImportAnimation()
  fun showWalletImportedAnimation()
  fun showError(type: ImportErrorType)
  fun hideAnimation()
}
