package com.asfoundation.wallet.permissions

import com.appcoins.wallet.permissions.PermissionName
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class PermissionsFragmentPresenter(
    private val view: PermissionFragmentView,
    private val permissionsInteractor: PermissionsInteractor,
    private val packageName: String,
    private val permissionName: PermissionName,
    private val apkSignature: String,
    private val disposables: CompositeDisposable, private val scheduler: Scheduler) {

  fun present() {
    handleAllowButtonClick()
  }

  private fun handleAllowButtonClick() {
    disposables.add(view.getAllowButtonClick().flatMapSingle {
      permissionsInteractor.grantPermission(packageName, apkSignature, permissionName)
    }.observeOn(scheduler).doOnNext { view.closeSuccess(it) }.subscribe()
    )
  }

  fun stop() {
    disposables.clear()
  }
}
