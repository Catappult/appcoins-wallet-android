package com.appcoins.wallet.permissions

data class ApplicationPermission(val walletAddress: String, val packageName: String,
                                 val apkSignature: String,
                                 val permissions: List<PermissionName>) {
  constructor(application: ApplicationPermission, permissions: List<PermissionName>) : this(
      application.walletAddress, application.packageName, application.apkSignature, permissions)
}