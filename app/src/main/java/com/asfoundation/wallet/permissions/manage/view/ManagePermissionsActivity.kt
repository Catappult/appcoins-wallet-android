package com.asfoundation.wallet.permissions.manage.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.wallet.appcoins.core.legacy_base.legacy.BaseActivity
import com.asf.wallet.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManagePermissionsActivity : com.wallet.appcoins.core.legacy_base.legacy.BaseActivity(), ManagePermissionsView, ToolbarManager {
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

  /**
   * function hardcoded temporarily, must be changed
   * @return
   */
  override fun toolbar(): Toolbar {
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    toolbar!!.visibility = View.VISIBLE
    if (toolbar != null) {
      setSupportActionBar(toolbar)
      toolbar.title = title
    }
    enableDisplayHomeAsUp()
    return toolbar
  }
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