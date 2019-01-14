package com.asfoundation.wallet.permissions

import com.appcoins.wallet.permissions.PermissionName

class PermissionsActivityPresenter(
    private val view: PermissionsActivityView, private val callingPackage: String,
    private val apkSignature: String,
    private val permission: PermissionName) {
  fun present(isCreating: Boolean) {
    if (isCreating) {
      view.showPermissionFragment(callingPackage, permission, apkSignature)
    }
  }

}
