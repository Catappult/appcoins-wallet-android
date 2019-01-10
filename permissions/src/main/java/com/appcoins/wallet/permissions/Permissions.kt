package com.appcoins.wallet.permissions

import com.appcoins.wallet.commons.Repository

class Permissions(private val repository: Repository<String, Application>) {

  /**
   * @throws SecurityException when trying to update the permission list of an apk and signature doesn't match
   */
  @Throws(SecurityException::class)
  fun grantPermission(packageName: String, apkSignature: String,
                      permission: PermissionName) {
    getApplication(packageName, apkSignature)?.let {
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
    return getApplication(packageName, apkSignature)?.permissions ?: return emptyList()
  }

  @Throws(SecurityException::class)
  private fun getApplication(packageName: String, apkSignature: String): Application? {
    val application = repository.getSync(packageName)
    if (application != null && (application.packageName != packageName || application.apkSignature != apkSignature)) {
      throw SecurityException("apk signature doesn't match")
    }
    return application
  }

  fun revokePermission(packageName: String, permissionName: PermissionName) {
    repository.getSync(packageName)?.let {
      val permissions = it.permissions.toMutableList()
      permissions.remove(permissionName)
      repository.saveSync(packageName, Application(it, permissions))
    }
  }
}
