package com.asfoundation.wallet.permissions

import com.appcoins.wallet.permissions.PermissionName
import com.appcoins.wallet.permissions.Permissions
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Completable
import io.reactivex.Single

class PermissionsInteractor(private val permissions: Permissions,
                            private val walletInteract: FindDefaultWalletInteract) {
  fun grantPermission(packageName: String, apkSignature: String,
                      permissionName: PermissionName): Single<String> {
    return walletInteract.find().flatMap {
      Completable.fromAction {
        permissions.grantPermission(it.address, packageName, apkSignature, permissionName)
      }
          .andThen(Single.just(it.address))
    }
  }
}
