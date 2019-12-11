package com.asfoundation.wallet.ui.balance

import io.reactivex.Observable

interface RemoveWalletView {

  fun backUpWalletClick(): Observable<Any>
  fun noBackUpWalletClick(): Observable<Any>
  fun navigateToBackUp()
  fun proceedWithRemoveWallet()
}
