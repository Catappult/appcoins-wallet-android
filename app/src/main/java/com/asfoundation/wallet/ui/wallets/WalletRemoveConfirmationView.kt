package com.asfoundation.wallet.ui.wallets

import io.reactivex.Observable

interface WalletRemoveConfirmationView {

  fun noButtonClick(): Observable<Any>

  fun yesButtonClick(): Observable<Any>

  fun navigateBack()

  fun showRemoveWalletAnimation()

  fun finish()

  fun showAuthentication()

  fun authenticationResult(): Observable<Boolean>
}
