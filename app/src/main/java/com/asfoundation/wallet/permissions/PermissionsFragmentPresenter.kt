package com.asfoundation.wallet.permissions

import com.appcoins.wallet.permissions.PermissionName
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class PermissionsFragmentPresenter(
    private val view: PermissionFragmentView,
    private val permissionsIntereactor: PermissionsInteractor,
    private val packageName: String,
    private val permissionName: PermissionName,
    private val apkSignature: String,
    private val disposables: CompositeDisposable, private val scheduler: Scheduler) {
  fun present(isCreating: Boolean) {
    handleAllowButtonClick()
  }

  private fun handleAllowButtonClick() {
    disposables.add(view.getAllowButtonClick().flatMapSingle {
      permissionsIntereactor.grantPermission(packageName, apkSignature, permissionName)
    }.observeOn(scheduler).doOnNext { view.closeSuccess(it) }.subscribe()
    )
  }
}
