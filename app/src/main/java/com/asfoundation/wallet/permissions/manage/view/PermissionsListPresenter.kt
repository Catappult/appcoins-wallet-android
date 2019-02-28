package com.asfoundation.wallet.permissions.manage.view

import com.appcoins.wallet.permissions.PermissionName
import com.asfoundation.wallet.permissions.PermissionsInteractor
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PermissionsListPresenter(private val view: PermissionsListView,
                               private val permissionsInteractor: PermissionsInteractor,
                               private val viewScheduler: Scheduler,
                               private val ioScheduler: Scheduler,
                               private val disposables: CompositeDisposable) {
  fun present() {
    showPermissionsList()
    handlePermissionClick()
  }

  private fun handlePermissionClick() {
    disposables.add(view.getPermissionClick()
        .observeOn(ioScheduler)
        .flatMapSingle {
          return@flatMapSingle if (it.hasPermission) {
            permissionsInteractor.grantPermission(it.packageName, it.apkSignature,
                PermissionName.WALLET_ADDRESS)
          } else {
            permissionsInteractor.revokePermission(it.packageName, PermissionName.WALLET_ADDRESS)
          }
        }
        .subscribe())
  }

  private fun showPermissionsList() {
    disposables.add(
        permissionsInteractor.getPermissions()
            .subscribeOn(Schedulers.io())
            .observeOn(viewScheduler)
            .flatMapCompletable {
              if (it.isEmpty()) {
                Completable.fromAction { view.showEmptyState() }
              } else {
                view.showPermissions(it)
              }
            }
            .subscribe())
  }

  fun stop() {
    disposables.clear()
  }

}
