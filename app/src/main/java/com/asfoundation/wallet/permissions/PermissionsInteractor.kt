package com.asfoundation.wallet.permissions

import com.appcoins.wallet.permissions.ApplicationPermission
import com.appcoins.wallet.permissions.PermissionName
import com.appcoins.wallet.permissions.Permissions
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import io.reactivex.Completable
import io.reactivex.Observable
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

  fun hasPermission(packageName: String, apkSignature: String,
                    permission: PermissionName): Single<Permission> {
    return walletInteract.find()
        .flatMap { wallet ->
          Single.just(permissions.getPermissions(wallet.address, packageName, apkSignature)).map {
            for (permissionName in it) {
              if (permissionName == permission) {
                return@map Permission(wallet.address, true)
              }
            }
            return@map Permission(wallet.address, false)
          }
        }
  }

  fun getWalletAddress(): Single<String> {
    return walletInteract.find().map { it.address }
  }

  fun getPermissions(): Observable<List<ApplicationPermission>> {
    return permissions.getPermissions()
  }
}
