package com.asfoundation.wallet.permissions

import android.os.Bundle
import com.appcoins.wallet.permissions.PermissionName
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity

class PermissionsActivity : BaseActivity(), PermissionsActivityView {

  companion object {
    private const val PERMISSION_NAME_KEY = "permission_name_key"
  }

  private lateinit var permissionsActivityPresenter: PermissionsActivityPresenter
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_permissions_layout)
    permissionsActivityPresenter =
        PermissionsActivityPresenter(this, callingPackage, getPermission())
    permissionsActivityPresenter.present(savedInstanceState == null)
  }

  private fun getPermission(): PermissionName {
    return PermissionName.valueOf(intent.extras[PERMISSION_NAME_KEY] as String)
  }

  override fun showPermissionFragment(callingPackage: String,
                                      permission: PermissionName) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            PermissionFragment.newInstance(callingPackage, permission))
        .commit()
  }

}