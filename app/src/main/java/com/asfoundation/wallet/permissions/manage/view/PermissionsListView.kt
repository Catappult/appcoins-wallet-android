package com.asfoundation.wallet.permissions.manage.view

import com.appcoins.wallet.permissions.ApplicationPermission
import io.reactivex.Completable

interface PermissionsListView {
  fun showPermissions(permissions: List<ApplicationPermission>): Completable

}
