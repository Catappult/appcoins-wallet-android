package com.asfoundation.wallet.permissions.manage.view

import com.appcoins.wallet.permissions.ApplicationPermission

interface PermissionsListView {
  fun showPermissions(permissions: List<ApplicationPermission>)

}
