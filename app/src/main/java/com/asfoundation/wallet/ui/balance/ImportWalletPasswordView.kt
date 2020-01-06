package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.ImportErrorType
import io.reactivex.Observable

interface ImportWalletPasswordView {

  fun updateUi(address: String, fiatValue: FiatValue)
  fun importWalletButtonClick(): Observable<String>
  fun showWalletImportAnimation()
  fun showWalletImportedAnimation()
  fun showError(type: ImportErrorType)
  fun hideAnimation()
}
