package com.asfoundation.wallet.permissions.request.view

import com.appcoins.wallet.permissions.PermissionName
import com.asfoundation.wallet.permissions.Permission
import com.asfoundation.wallet.permissions.PermissionsInteractor
import com.asfoundation.wallet.repository.WalletNotFoundException
import io.reactivex.Scheduler
import io.reactivex.Single
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
    setupUi(isCreating)
    handleWalletCreationFinishEvent()
  }

  private fun handleWalletCreationFinishEvent() {
    view.getWalletCreatedEvent().flatMapSingle { showPermissionsScreen() }.subscribe()
  }

  private fun setupUi(isCreating: Boolean) {
    if (isCreating) {
      disposables.add(
          showPermissionsScreen().subscribe({}, {
            when (it) {
              is WalletNotFoundException -> view.showWalletCreation()
            }
          }))
    }
  }

  private fun showPermissionsScreen(): Single<Permission> {
    return permissionsInteractor.hasPermission(callingPackage, apkSignature, permissionName)
        .observeOn(viewScheduler)
        .doOnSuccess { permission ->
          if (permission.permissionGranted) {
            view.closeSuccess(permission.walletAddress)
          } else {
            view.showPermissionFragment(callingPackage, permissionName, apkSignature)
          }
        }
  }

  fun stop() {
    disposables.clear()
  }


}
