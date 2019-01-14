package com.asfoundation.wallet.permissions

import io.reactivex.Observable

interface PermissionFragmentView {
  fun getAllowButtonClick(): Observable<Any>
  fun closeSuccess(walletAddress: String)
}
