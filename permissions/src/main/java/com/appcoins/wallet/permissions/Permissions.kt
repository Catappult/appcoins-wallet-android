package com.appcoins.wallet.permissions

import com.appcoins.wallet.commons.Repository

class Permissions(private val repository: Repository<String, Application>) {

  /**
   * @throws SecurityException when trying to update the permission list of an apk and signature doesn't match
   */
  @Throws(SecurityException::class)
  fun grantPermission(packageName: String, apkSignature: String,
                      permission: PermissionName) {
    repository.getSync(packageName)?.let {
      if (it.apkSignature != apkSignature) {
        throw SecurityException("apk signature doesn't match")
      }
      val oldPermissions = it.permissions.toMutableList()
      oldPermissions.add(permission)
      repository.saveSync(packageName,
          Application(it, oldPermissions.toList()))
    } ?: repository.saveSync(packageName,
        Application(packageName, apkSignature, listOf(permission)))
  }

  /**
   * @throws SecurityException when trying to get the permission list of an apk with a wrong signature
   */
  @Throws(SecurityException::class)
  fun getPermissions(packageName: String, apkSignature: String): List<PermissionName> {
    val permission = repository.getSync(packageName) ?: return emptyList()
    if (permission.packageName != packageName || permission.apkSignature != apkSignature) {
      throw SecurityException("apk signature doesn't match")
    }
    return permission.permissions
  }
}
