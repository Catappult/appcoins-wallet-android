package com.asfoundation.wallet.permissions.manage.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import dagger.android.support.DaggerFragment

class PermissionsListFragment : DaggerFragment() {
  companion object {
    fun newInstance(): Fragment {
      return PermissionsListFragment()
    }
  }

  private lateinit var presenter: PermissionsListPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = PermissionsListPresenter()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

}
