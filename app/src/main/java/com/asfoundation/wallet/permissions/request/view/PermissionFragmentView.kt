package com.asfoundation.wallet.permissions.request.view

import io.reactivex.Observable

interface PermissionFragmentView {
  fun getAllowButtonClick(): Observable<Any>
  fun closeSuccess(walletAddress: String)
  fun getAllowOnceClick(): Observable<Any>
  fun getCancelClick(): Observable<Any>
  fun closeCancel()
  fun showAppData(packageName: String)
  fun showWalletAddress(wallet: String)
}
