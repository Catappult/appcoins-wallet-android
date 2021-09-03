package com.asfoundation.wallet.restore.password

import com.asfoundation.wallet.util.RestoreErrorType
import io.reactivex.Observable

interface RestoreWalletPasswordView {

  fun updateUi(address: String, fiatAmount: String, fiatSymbol: String)

  fun restoreWalletButtonClick(): Observable<String>

  fun showWalletRestoreAnimation()

  fun showWalletRestoredAnimation()

  fun showError(type: RestoreErrorType)

  fun hideAnimation()

  fun hideKeyboard()
}
