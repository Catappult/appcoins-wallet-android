package com.asfoundation.wallet.permissions.manage.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity

class ManagePermissionsActivity : BaseActivity(), ManagePermissionsView, ToolbarManager {
  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, ManagePermissionsActivity::class.java)
    }
  }

  override fun setupToolbar() {
    setTitle(R.string.permissions_title)
    toolbar()
  }

  private lateinit var presenter: ManagePermissionsPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_permissions_layout)
    presenter = ManagePermissionsPresenter(this)
    presenter.present(savedInstanceState == null)
  }

  override fun showPermissionsList() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, PermissionsListFragment.newInstance()).commit()
  }
}