package com.appcoins.wallet.permissions

data class Application(val packageName: String, val apkSignature: String,
                       val permissions: List<PermissionName>) {
  constructor(application: Application, permissions: List<PermissionName>) : this(
      application.packageName, application.apkSignature, permissions)
}