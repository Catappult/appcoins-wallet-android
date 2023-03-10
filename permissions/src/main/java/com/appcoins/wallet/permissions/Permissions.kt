package com.appcoins.wallet.permissions

import com.appcoins.wallet.commons.Repository
import io.reactivex.Observable

class Permissions(private val repository: Repository<String, ApplicationPermission>) {

  /**
   * @throws SecurityException when trying to update the permission list of an apk and signature doesn't match
   */
  @Throws(SecurityException::class)
  fun grantPermission(walletAddress: String, packageName: String,
                      apkSignature: String,
                      permission: PermissionName) {
    getApplication(walletAddress, packageName, apkSignature)?.let {
      val oldPermissions = it.permissions.toMutableSet()
      oldPermissions.add(permission)
      saveApplicationPermission(walletAddress, packageName,
          ApplicationPermission(it, oldPermissions.toList()))
    } ?: saveApplicationPermission(walletAddress, packageName,
        ApplicationPermission(walletAddress, packageName, apkSignature, listOf(permission)))
  }

  private fun saveApplicationPermission(walletAddress: String, packageName: String,
                                        applicationPermission: ApplicationPermission) {
    repository.saveSync(getKey(walletAddress, packageName), applicationPermission)
  }

  /**
   * @throws SecurityException when trying to get the permission list of an apk with a wrong signature
   */
  @Throws(SecurityException::class)
  fun getPermissions(walletAddress: String, packageName: String,
                     apkSignature: String): List<PermissionName> {
    return getApplication(walletAddress, packageName, apkSignature)?.permissions
        ?: return emptyList()
  }

  @Throws(SecurityException::class)
  private fun getApplication(walletAddress: String, packageName: String,
                             apkSignature: String): ApplicationPermission? {
    val application = getApplicationPermission(walletAddress, packageName)
    if (application != null && (application.packageName != packageName || application.apkSignature != apkSignature)) {
      throw SecurityException("apk signature doesn't match")
    }
    return application
  }

  fun revokePermission(walletAddress: String, packageName: String, permissionName: PermissionName) {
    getApplicationPermission(walletAddress, packageName)?.let {
      val permissions = it.permissions.toMutableList()
      permissions.remove(permissionName)
      saveApplicationPermission(walletAddress, packageName, ApplicationPermission(it, permissions))
    }
  }

  private fun getApplicationPermission(walletAddress: String,
                                       packageName: String) =
      repository.getSync(getKey(walletAddress, packageName))

  private fun getKey(walletAddress: String, packageName: String) =
      walletAddress + packageName

  fun getPermissions(walletAddress: String): Observable<List<ApplicationPermission>> {
    return repository.all.flatMapSingle {
      Observable.fromIterable(it).filter { permission -> permission.walletAddress == walletAddress }
          .toList()
    }
  }
}
