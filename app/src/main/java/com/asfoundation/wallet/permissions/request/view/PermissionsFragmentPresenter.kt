package com.asfoundation.wallet.permissions.request.view

import com.appcoins.wallet.permissions.PermissionName
import com.asfoundation.wallet.permissions.PermissionsInteractor
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class PermissionsFragmentPresenter(
    private val view: PermissionFragmentView,
    private val permissionsInteractor: PermissionsInteractor,
    private val packageName: String,
    private val permissionName: PermissionName,
    private val apkSignature: String,
    private val disposables: CompositeDisposable, private val viewScheduler: Scheduler) {

  fun present() {
    handleAllowButtonClick()
    handleAllowOnceClick()
    handleCancelClick()
    setupUi()
  }

  private fun setupUi() {
    disposables.add(
        permissionsInteractor.getWalletAddress()
            .observeOn(viewScheduler)
            .subscribe { wallet ->
              view.showWalletAddress(wallet)
            })

    view.showAppData(packageName)
  }

  private fun handleCancelClick() {
    disposables.add(
        view.getCancelClick().doOnNext { view.closeCancel() }
            .subscribe())
  }

  private fun handleAllowOnceClick() {
    disposables.add(
        view.getAllowOnceClick().flatMapSingle {
          permissionsInteractor.getWalletAddress()
              .observeOn(viewScheduler)
        }.doOnNext { view.closeSuccess(it) }
            .subscribe())
  }

  private fun handleAllowButtonClick() {
    disposables.add(view.getAllowButtonClick()
        .flatMapSingle {
          permissionsInteractor.grantPermission(packageName, apkSignature, permissionName)
        }.observeOn(viewScheduler)
        .doOnNext { view.closeSuccess(it) }
        .subscribe())
  }

  fun stop() {
    disposables.clear()
  }
}
