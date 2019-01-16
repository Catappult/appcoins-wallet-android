package com.asfoundation.wallet.permissions

import io.reactivex.Observable

interface CreateWalletView {
  fun getOnCreateWalletClick(): Observable<Any>
  fun closeSuccess()
  fun getCancelClick(): Observable<Any>
  fun closeCancel()
}
