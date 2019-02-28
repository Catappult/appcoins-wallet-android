package com.asfoundation.wallet.permissions.manage.view

import com.appcoins.wallet.permissions.ApplicationPermission
import io.reactivex.Completable
import io.reactivex.Observable

interface PermissionsListView {
  fun showPermissions(permissions: List<ApplicationPermission>): Completable
  fun getPermissionClick(): Observable<ApplicationPermissionToggle>
  fun showEmptyState()

  data class ApplicationPermissionToggle(val packageName: String,
                                         val hasPermission: Boolean,
                                         val apkSignature: String)
}
