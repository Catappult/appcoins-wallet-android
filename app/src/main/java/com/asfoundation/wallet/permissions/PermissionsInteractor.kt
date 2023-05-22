package com.asfoundation.wallet.permissions

import com.appcoins.wallet.permissions.ApplicationPermission
import com.appcoins.wallet.permissions.PermissionName
import com.appcoins.wallet.permissions.Permissions
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class PermissionsInteractor @Inject constructor(private val permissions: Permissions,
                                                private val walletInteract: com.appcoins.wallet.feature.walletInfo.data.FindDefaultWalletInteract) {

  fun grantPermission(packageName: String, apkSignature: String,
                      permissionName: PermissionName): Single<String> {
    return walletInteract.find()
        .flatMap {
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
          Single.just(permissions.getPermissions(wallet.address, packageName, apkSignature))
              .map {
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
    return walletInteract.find()
        .map { it.address }
  }

  fun getPermissions(): Observable<List<ApplicationPermission>> {
    return getWalletAddress().flatMapObservable { permissions.getPermissions(it) }
  }

  fun revokePermission(packageName: String, permissionName: PermissionName): Single<Wallet> {
    return walletInteract.find()
        .doOnSuccess { permissions.revokePermission(it.address, packageName, permissionName) }
  }
}
