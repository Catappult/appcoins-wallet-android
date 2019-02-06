package com.asfoundation.wallet.permissions.manage.view

class ManagePermissionsPresenter(private val view: ManagePermissionsView) {
  fun present(isCreating: Boolean) {
    if (isCreating) {
      view.showPermissionsList()
    }
  }
}
