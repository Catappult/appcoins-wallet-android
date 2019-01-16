package com.asfoundation.wallet.permissions

import com.appcoins.wallet.permissions.PermissionName
import com.asfoundation.wallet.repository.WalletNotFoundException
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class PermissionsActivityPresenter(
    private val view: PermissionsActivityView,
    private val permissionsInteractor: PermissionsInteractor,
    private val callingPackage: String,
    private val apkSignature: String,
    private val permissionName: PermissionName,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler) {
  fun present(isCreating: Boolean) {
    if (isCreating) {
      disposables.add(
          permissionsInteractor.hasPermission(callingPackage, apkSignature, permissionName)
              .observeOn(viewScheduler)
              .doOnSuccess { permission ->
                if (permission.permissionGranted) {
                  view.closeSuccess(permission.walletAddress)
                } else {
                  view.showPermissionFragment(callingPackage, permissionName, apkSignature)
                }
              }.subscribe({}, {
                when (it) {
                  is WalletNotFoundException -> view.showWalletCreation()
                }
              }))
    }
  }

  fun stop() {
    disposables.clear()
  }


}
