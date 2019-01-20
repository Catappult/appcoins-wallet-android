package com.asfoundation.wallet.permissions.request.view

import com.appcoins.wallet.permissions.PermissionName
import io.reactivex.Observable

interface PermissionsActivityView {
  fun showPermissionFragment(callingPackage: String, permission: PermissionName,
                             apkSignature: String)

  fun closeSuccess(walletAddress: String)
  fun showWalletCreation()
  fun getWalletCreatedEvent(): Observable<Any>
}
