package com.asfoundation.wallet.ui.balance

import io.reactivex.Observable

interface WalletRemoveConfirmationView {

  fun noButtonClick(): Observable<Any>
  fun yesButtonClick(): Observable<Any>
  fun navigateBack()
  fun showRemoveWalletAnimation()
  fun finish()
}
