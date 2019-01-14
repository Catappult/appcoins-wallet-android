package com.asfoundation.wallet.permissions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appcoins.wallet.permissions.PermissionName
import com.asf.wallet.R
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class PermissionFragment : DaggerFragment() {
  companion object {
    private const val CALLING_PACKAGE = "calling_package_key"
    private const val PERMISSION_KEY = "permission_key"

    fun newInstance(callingPackage: String,
                    permission: PermissionName): PermissionFragment {

      return PermissionFragment().apply {
        arguments = Bundle().apply {
          putString(CALLING_PACKAGE, callingPackage)
          putSerializable(PERMISSION_KEY, permission)
        }
      }
    }
  }

  @Inject
  lateinit var permissionsInteractor: PermissionsInteractor
  private lateinit var presenter: PermissionsFragmentPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = PermissionsFragmentPresenter(permissionsInteractor)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_permissions_layout, container, false)
  }
}
